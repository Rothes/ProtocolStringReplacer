package io.github.rothes.protocolstringreplacer.console

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig
import io.github.rothes.protocolstringreplacer.api.user.PsrUser
import io.github.rothes.protocolstringreplacer.replacer.ListenType
import io.github.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.appender.AbstractAppender
import org.apache.logging.log4j.core.config.AbstractConfiguration
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.layout.PatternLayout
import org.apache.logging.log4j.core.lookup.Interpolator
import org.apache.logging.log4j.core.lookup.StrLookup
import org.apache.logging.log4j.core.lookup.StrSubstitutor
import org.apache.logging.log4j.core.pattern.PatternFormatter
import org.apache.logging.log4j.core.pattern.PatternParser
import org.apache.logging.log4j.spi.AbstractLogger
import org.bukkit.Bukkit
import org.jline.reader.LineReader
import org.w3c.dom.Node
import java.io.InputStream
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.function.BiPredicate
import java.util.logging.Filter
import java.util.logging.LogRecord
import java.util.logging.SimpleFormatter
import javax.xml.parsers.DocumentBuilder

class ConsoleReplaceManager(private val plugin: ProtocolStringReplacer) {

    private var psrFilter: PsrFilter? = null
    private var oldFactory: Any? = null
    private var ognJndiLkup: Any? = null
    private var canReplacePatterns = false
    private var isLegacy = false

    fun initialize() {
        val context = LogManager.getContext(false) as LoggerContext
        val config = context.configuration
        try {
            tryReplaceLogPatterns(config)
            replaceReader(config, false)
            canReplacePatterns = true
        } catch (e: Throwable) {
            e.printStackTrace()
            PsrMessage.initialize(plugin)
            // This is for plugin Logger and server things.
            Bukkit.getServer().logger.parent.handlers[0].formatter = object : SimpleFormatter() {
                override fun formatMessage(record: LogRecord): String {
                    val string = super.formatMessage(record)
                    if (string == null || !plugin.hasStarted()) {
                        return string
                    }
                    val container = SimpleTextContainer(string)
                    container.createTexts(container)
                    val replacerManager = plugin.replacerManager
                    val consoleUser = plugin.userManager.consoleUser
                    val replacers = replacerManager.getAcceptedReplacers(consoleUser, filter)
                    replacerManager.replaceContainerTexts(container, replacers)
                    if (plugin.configManager.consolePlaceholder) {
                        replacerManager.setPapi(consoleUser, container.texts)
                    }
                    return container.result
                }
            }
            Bukkit.getServer().logger.parent.handlers[0].filter = Filter { record: LogRecord ->
                if (!plugin.hasStarted()) {
                    return@Filter true
                }
                val container = SimpleTextContainer(record.message)
                container.createTexts(container)
                val replacerManager = plugin.replacerManager
                val replacers = replacerManager.getAcceptedReplacers(plugin.userManager.consoleUser, filter)
                !replacerManager.isTextBlocked(container, replacers)
            }

            // This is for Sender#sendMessage and server things.
            try {
                val field = AbstractLogger::class.java.getDeclaredField("messageFactory").ac()
                oldFactory = field[LogManager.getRootLogger()]
                field[LogManager.getRootLogger()] = PsrMessageFactory()
            } catch (ex: NoSuchFieldException) {
                e.printStackTrace()
            } catch (ex: IllegalAccessException) {
                e.printStackTrace()
            }
            psrFilter = PsrFilter(plugin)
            (LogManager.getRootLogger() as Logger).addFilter(psrFilter)
        }

        fixJndi(config, false)
    }

    fun disable() {
        val context = LogManager.getContext(false) as LoggerContext
        val config = context.configuration
        if (canReplacePatterns) {
            // Get the default xml configuration from server jar.
            val appenders = getAppendersNode(config)

            // Remove PSR converter.
            getConverters(config).remove("PsrFormatting")

            processAppenders(config, appenders, true)
            replaceReader(config, true)
        } else {
            Bukkit.getServer().logger.parent.handlers[0].formatter = SimpleFormatter()
            Bukkit.getServer().logger.parent.handlers[0].filter = null

            val field = AbstractLogger::class.java.getDeclaredField("messageFactory").ac()
            field[LogManager.getRootLogger()] = oldFactory
            config.removeFilter(psrFilter)
        }

        fixJndi(config, true)
    }

    private fun replaceReader(config: Configuration, restore: Boolean) {
        try {
            val terminalconsole = getPluginClass(config, "terminalconsole") ?: return
            val reader = terminalconsole.getDeclaredMethod("getReader").invoke(null) as LineReader
            terminalconsole.getDeclaredMethod("setReader", LineReader::class.java).invoke(
                null, if (restore) (reader as PsrWrappedLineReader).oriReader else PsrWrappedLineReader(reader)
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
    }

    private fun tryReplaceLogPatterns(config: Configuration) {
        // Get the default xml configuration from server jar.
        val appenders = getAppendersNode(config)

        // Add PSR converter.
        getConverters(config)["PsrFormatting"] = PsrLogEventPatternConverter::class.java

        processAppenders(config, appenders, false)
        patterns.clear()
    }

    private fun processAppenders(config: Configuration, appenders: Node, restore: Boolean) {
        val childNodes = appenders.childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeName == "#text") {
                continue
            }
            val nameNode = node.attributes.getNamedItem("name") ?: return
            val name = nameNode.nodeValue
            var removeAnsi = false
            when (node.nodeName) {
                "RollingRandomAccessFile",
                "ServerGuiConsole" -> {
                    removeAnsi = true
                }
                "Queue" -> {
                    if (!node.hasAttributes()) {
                        break
                    }
                    val nameAttr = node.attributes.getNamedItem("name")
                    if (nameAttr != null && nameAttr.nodeValue == "ServerGuiConsole") {
                        removeAnsi = true
                    }
                }
                else -> {}
            }
            val appenderNode = node.child("PatternLayout")
            if (appenderNode != null) {
                setAppender(config, appenderNode, name, removeAnsi, restore)
            }
        }
    }

    private fun setAppender(
        config: Configuration, appenderNode: Node, appenderName: String, removeAnsi: Boolean, restore: Boolean
    ) {
        val loggerNamePatternSelector = getPluginClass(config, "loggernamepatternselector")

        val appender = config.appenders[appenderName]
        if (appender == null) {
            ProtocolStringReplacer.warn("§4Appender \"$appenderName\" is null, ignoring.")
            return
        }
        val layout = AbstractAppender::class.java.getDeclaredField("layout").ac()[appender] as PatternLayout
        val selectorNode = appenderNode.child("LoggerNamePatternSelector")
        var index = patterns.size
        val parser = PatternLayout.createPatternParser(config)
        if (selectorNode != null && loggerNamePatternSelector != null) {
            val defaultPattern = selectorNode.attributes.getNamedItem("defaultPattern").nodeValue
            patterns.add(defaultPattern)

            val selector = PatternLayout::class.java.getDeclaredField("patternSelector").ac()[layout]

            loggerNamePatternSelector.getDeclaredField("defaultFormatters").ac()[selector] =
                parser.parse(if (restore) defaultPattern else ("%PsrFormatting{" + index + "}" + (if (removeAnsi) "{removeAnsi}" else "")))
                    .toTypedArray<PatternFormatter>()

            val patternMatch = selectorNode.child("PatternMatch") ?: return
            val pattern = patternMatch.attributes.getNamedItem("pattern").nodeValue
            @Suppress("UNCHECKED_CAST")
            val formatters = loggerNamePatternSelector.getDeclaredField("formatters").ac()[selector] as List<Any>
            for (formatter in formatters) {
                patterns.add(pattern)
                formatter.javaClass.getDeclaredField("formatters").ac()[formatter] =
                    parser.parse(if (restore) pattern else ("%PsrFormatting{" + ++index + "}" + (if (removeAnsi) "{removeAnsi}" else "")))
                        .toTypedArray<PatternFormatter>()
            }
        } else {
            val field = PatternLayout::class.java.getDeclaredField("conversionPattern").ac()
            val defaultPattern = field[layout] as String
            patterns.add(defaultPattern)
            val pattern = if (restore) appenderNode.attributes.getNamedItem("pattern").nodeValue
            else ("%PsrFormatting{" + index + "}" + (if (removeAnsi) "{removeAnsi}" else ""))
            field[layout] = pattern

            if (isLegacy) {
                layout.javaClass.getDeclaredField("formatters").ac()[layout] = parser.parse(pattern)
            } else {
                PatternLayout::class.java.getDeclaredField("eventSerializer").ac()[layout] =
                    PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(pattern)
                        .setDefaultPattern(pattern).build()
            }
        }
    }

    private fun getAppendersNode(config: Configuration): Node {
        val xmlConfigurationClass = try {
            Class.forName("org.apache.logging.log4j.core.config.xml.XmlConfiguration")
        } catch (ignored: ClassNotFoundException) {
            try {
                Class.forName("org.apache.logging.log4j.core.config.XMLConfiguration")
            } catch (e: ClassNotFoundException) {
                error("Log4j not supported")
            }
        }
        val method = try {
            xmlConfigurationClass.getDeclaredMethod("newDocumentBuilder", Boolean::class.javaPrimitiveType)
        } catch (e: NoSuchMethodException) {
            isLegacy = true
            xmlConfigurationClass.getDeclaredMethod("newDocumentBuilder")
        }.also { it.ac() }
        val builder: DocumentBuilder
        val inputStream: InputStream
        if (!isLegacy) {
            builder = method.invoke(config, true) as DocumentBuilder
            inputStream = config.configurationSource.resetInputStream().inputStream
        } else {
            builder = method.invoke(config) as DocumentBuilder
            inputStream = config.javaClass.classLoader.getResourceAsStream("log4j2.xml")!!
        }

        val document = inputStream.use { builder.parse(it) }
        val element = document.documentElement
        return element.child("Appenders")!!
    }

    private fun getConverters(config: Configuration): MutableMap<String, Class<*>> {
        try {
            config.pluginPackages.add("io.github.rothes.protocolstringreplacer.console")
        } catch (ignored: NoSuchMethodError) { }

        val patternParser = PatternLayout.createPatternParser(config)
        val field = PatternParser::class.java.getDeclaredField("converterRules").ac()
        @Suppress("UNCHECKED_CAST")
        return field[patternParser] as MutableMap<String, Class<*>>
    }

    private fun Node.child(childName: String): Node? {
        val childNodes = childNodes
        for (i in 0 until childNodes.length) {
            val node = childNodes.item(i)
            if (node.nodeName == childName) {
                return node
            }
        }
        return null
    }

    private fun getPluginClass(config: Configuration, name: String): Class<*>? {
        val map: Map<String?, Any> = try {
            (config as AbstractConfiguration).pluginManager.plugins
        } catch (e: NoClassDefFoundError) {
            val clazz = Class.forName("org.apache.logging.log4j.core.config.BaseConfiguration")
            val pluginManager = clazz.getDeclaredField("pluginManager").ac()[config]
            @Suppress("UNCHECKED_CAST")
            pluginManager.javaClass.getDeclaredMethod("getPlugins").ac().invoke(pluginManager) as Map<String?, Any>
        }
        val get = map[name] ?: return null
        return get.javaClass.getDeclaredMethod("getPluginClass").ac().invoke(get) as Class<*>
    }

    private fun fixJndi(config: Configuration, restore: Boolean) {
        var field = try {
            config.javaClass.superclass.getDeclaredField("subst")
        } catch (e: NoSuchFieldException) {
            config.javaClass.superclass.getDeclaredField("runtimeStrSubstitutor")
        }.also { it.ac() }
        val substitutor = field[config] as StrSubstitutor
        val interpolator = substitutor.variableResolver as Interpolator ?: return
        field = try {
            interpolator.javaClass.getDeclaredField("strLookupMap")
        } catch (e: NoSuchFieldException) {
            interpolator.javaClass.getDeclaredField("lookups")
        }.also { it.ac() }
        @Suppress("UNCHECKED_CAST")
        val pluginsMap = field[interpolator] as MutableMap<String, StrLookup>
        if (restore) {
            if (ognJndiLkup is StrLookup) {
                pluginsMap["jndi"] = ognJndiLkup as StrLookup
            } else {
                pluginsMap.remove("jndi")
            }
        } else {
            ognJndiLkup = pluginsMap["jndi"]
            pluginsMap["jndi"] = PsrJndiLookup()
        }
    }

    private fun Field.ac(): Field {
        isAccessible = true
        return this
    }

    private fun Method.ac(): Method {
        isAccessible = true
        return this
    }

    companion object {

        @JvmStatic
        val filter: BiPredicate<ReplacerConfig, PsrUser> =
            BiPredicate { replacerConfig: ReplacerConfig, user: PsrUser? ->
                replacerConfig.isEnabled && replacerConfig.listenTypeList.contains(ListenType.CONSOLE)
            }
        private val patterns: MutableList<String> = ArrayList()

        @JvmStatic
        fun getPatterns(): List<String> {
            return patterns
        }
    }
}
