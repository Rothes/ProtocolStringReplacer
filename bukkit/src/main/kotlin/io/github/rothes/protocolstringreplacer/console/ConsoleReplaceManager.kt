package io.github.rothes.protocolstringreplacer.console;

import io.github.rothes.protocolstringreplacer.ProtocolStringReplacer;
import io.github.rothes.protocolstringreplacer.api.replacer.ReplacerConfig;
import io.github.rothes.protocolstringreplacer.api.user.PsrUser;
import io.github.rothes.protocolstringreplacer.replacer.ListenType;
import io.github.rothes.protocolstringreplacer.replacer.ReplacerManager;
import io.github.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.lookup.Interpolator;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.core.lookup.StrSubstitutor;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.LineReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public final class ConsoleReplaceManager {

    private static final BiPredicate<ReplacerConfig, PsrUser> filter = (replacerConfig, user) ->
            replacerConfig.isEnabled() && replacerConfig.getListenTypeList().contains(ListenType.CONSOLE);
    private static final List<String> patterns = new ArrayList<>();
    private final ProtocolStringReplacer plugin;
    private PsrFilter psrFilter;
    private Object oriFactory;
    private Object oriJndiLkup;
    private boolean canReplacePatterns = false;
    private boolean isLegacy = false;

    public ConsoleReplaceManager(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
    }

    public static BiPredicate<ReplacerConfig, PsrUser> getFilter() {
        return filter;
    }

    public static @NotNull List<String> getPatterns() {
        return patterns;
    }

    public void initialize() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        try {
            tryReplaceLogPatterns(config);
            replaceReader(config, false);
            canReplacePatterns = true;
        } catch (Throwable e) {
            e.printStackTrace();
            PsrMessage.initialize(plugin);
            // This is for plugin Logger and server things.
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter(){
                @Override
                public String formatMessage(LogRecord record) {
                    String string = super.formatMessage(record);
                    if (string == null || !plugin.hasStarted()) {
                        return string;
                    }
                    SimpleTextContainer container = new SimpleTextContainer(string);
                    container.createTexts(container);
                    ReplacerManager replacerManager = plugin.getReplacerManager();
                    PsrUser consoleUser = plugin.getUserManager().getConsoleUser();
                    List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(consoleUser, filter);
                    replacerManager.replaceContainerTexts(container, replacers);
                    if (plugin.getConfigManager().consolePlaceholder) {
                        replacerManager.setPapi(consoleUser, container.getTexts());
                    }
                    return container.getResult();
                }
            });
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFilter(record -> {
                if (!plugin.hasStarted()) {
                    return true;
                }
                SimpleTextContainer container = new SimpleTextContainer(record.getMessage());
                container.createTexts(container);
                ReplacerManager replacerManager = plugin.getReplacerManager();
                List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(plugin.getUserManager().getConsoleUser(), filter);
                return !replacerManager.isTextBlocked(container, replacers);
            });

            // This is for Sender#sendMessage and server things.
            try {
                Field field = AbstractLogger.class.getDeclaredField("messageFactory");
                field.setAccessible(true);
                oriFactory = field.get(LogManager.getRootLogger());
                PsrMessageFactory psrMessageFactory = new PsrMessageFactory();
                field.set(LogManager.getRootLogger(), psrMessageFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException ex) {
                e.printStackTrace();
            }
            psrFilter = new PsrFilter(plugin);
            ((Logger) LogManager.getRootLogger()).addFilter(psrFilter);
        }

        fixJndi(config, false);
    }

    public void disable() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        if (canReplacePatterns) {
            // Get the default xml configuration from server jar.
            Node appenders = getAppendersNode(config);

            // Remove PSR converter.
            getConverters(config).remove("PsrFormatting");

            processAppenders(config, appenders, true);
            replaceReader(config, true);

        } else {
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter());
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFilter(null);
            try {
                Field field = AbstractLogger.class.getDeclaredField("messageFactory");
                field.setAccessible(true);
                field.set(LogManager.getRootLogger(), oriFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            config.removeFilter(psrFilter);

        }

        fixJndi(config, true);
    }

    private void replaceReader(Configuration config, boolean restore) {
        try {
            Class<?> terminalconsole = getPluginClass(config, "terminalconsole");
            if (terminalconsole == null) {
                return;
            }
            LineReader reader = (LineReader) terminalconsole.getDeclaredMethod("getReader").invoke(null);
            if (reader == null) {
                return;
            }
            terminalconsole.getDeclaredMethod("setReader", LineReader.class).invoke(null,
                    restore ? ((PsrWrappedLineReader) reader).getOriReader() : new PsrWrappedLineReader(reader));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void tryReplaceLogPatterns(Configuration config) {
        // Get the default xml configuration from server jar.
        Node appenders = getAppendersNode(config);

        // Add PSR converter.
        getConverters(config).put("PsrFormatting", PsrLogEventPatternConverter.class);

        processAppenders(config, appenders, false);
        patterns.clear();
    }

    private void processAppenders(Configuration config, Node appenders, boolean restore) {
        NodeList childNodes = appenders.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeName().equals("#text")) {
                continue;
            }
            Node nameNode = item.getAttributes().getNamedItem("name");
            if (nameNode == null) {
                return;
            }
            String name = nameNode.getNodeValue();
            boolean removeAnsi = false;
            switch (item.getNodeName()) {
                case "RollingRandomAccessFile":
                case "ServerGuiConsole":
                    removeAnsi = true;
                    break;
                case "Queue":
                    if (!item.hasAttributes()) {
                        break;
                    }
                    Node nameAttr = item.getAttributes().getNamedItem("name");
                    if (nameAttr != null && nameAttr.getNodeValue().equals("ServerGuiConsole")) {
                        removeAnsi = true;
                    }
                default:
                    break;
            }
            Node appenderNode = getChild(item, "PatternLayout");
            if (appenderNode != null) {
                setAppender(config, appenderNode, name, removeAnsi, restore);
            }
        }
    }

    private void setAppender(Configuration config, Node appenderNode, String appenderName, boolean removeAnsi, boolean restore) {
        Class<?> loggerNamePatternSelector = getPluginClass(config, "loggernamepatternselector");
        try {
            Field field;
            field = AbstractAppender.class.getDeclaredField("layout");
            field.setAccessible(true);
            Appender appender = config.getAppenders().get(appenderName);
            if (appender == null) {
                ProtocolStringReplacer.error("§4Appender \"" + appenderName + "\" is null, ignoring.");
                return;
            }
            PatternLayout layout = (PatternLayout) field.get(appender);
            Node selectorNode = getChild(appenderNode, "LoggerNamePatternSelector");
            int index = patterns.size();
            PatternParser parser = PatternLayout.createPatternParser(config);
            if (selectorNode != null && loggerNamePatternSelector != null) {
                String defaultPattern = selectorNode.getAttributes().getNamedItem("defaultPattern").getNodeValue();

                field = PatternLayout.class.getDeclaredField("patternSelector");
                field.setAccessible(true);
                Object selector = field.get(layout);
                field.setAccessible(false);

                field = loggerNamePatternSelector.getDeclaredField("defaultFormatters");
                field.setAccessible(true);
                patterns.add(defaultPattern);
                field.set(selector, parser.parse(restore ? defaultPattern : ("%PsrFormatting{" + index + "}" + (removeAnsi ? "{removeAnsi}" : ""))).toArray(new PatternFormatter[0]));
                field.setAccessible(false);

                Node patternMatch = getChild(selectorNode, "PatternMatch");
                if (patternMatch == null) {
                    return;
                }
                String pattern = patternMatch.getAttributes().getNamedItem("pattern").getNodeValue();
                field = loggerNamePatternSelector.getDeclaredField("formatters");
                field.setAccessible(true);
                List<?> formatters = (List<?>) field.get(selector);
                field.setAccessible(false);
                for (Object formatter : formatters) {
                    field = formatter.getClass().getDeclaredField("formatters");
                    field.setAccessible(true);
                    patterns.add(pattern);
                    field.set(formatter, parser.parse(restore ? pattern : ("%PsrFormatting{" + ++index + "}" + (removeAnsi ? "{removeAnsi}" : ""))).toArray(new PatternFormatter[0]));
                    field.setAccessible(false);
                }

            } else {
                field = PatternLayout.class.getDeclaredField("conversionPattern");
                field.setAccessible(true);
                String defaultPattern = (String) field.get(layout);
                patterns.add(defaultPattern);
                String pattern = restore ? appenderNode.getAttributes().getNamedItem("pattern").getNodeValue()
                        : ("%PsrFormatting{" + index + "}" + (removeAnsi ? "{removeAnsi}" : ""));
                field.set(layout, pattern);
                field.setAccessible(false);
                if (isLegacy) {
                    field = layout.getClass().getDeclaredField("formatters");
                    field.setAccessible(true);
                    field.set(layout, parser.parse(pattern));
                } else {
                    field = PatternLayout.class.getDeclaredField("eventSerializer");
                    field.setAccessible(true);
                    field.set(layout, PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(pattern).setDefaultPattern(pattern).build());
                    field.setAccessible(false);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private Node getAppendersNode(Configuration config) {
        try {
            Class<?> xmlConfigurationClass;
            try {
                xmlConfigurationClass = Class.forName("org.apache.logging.log4j.core.config.xml.XmlConfiguration");
            } catch (ClassNotFoundException ignored) {
                try {
                    xmlConfigurationClass = Class.forName("org.apache.logging.log4j.core.config.XMLConfiguration");
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    return null;
                }
            }
            Method method;
            try {
                method = xmlConfigurationClass.getDeclaredMethod("newDocumentBuilder", boolean.class);
            } catch (NoSuchMethodException e) {
                method = xmlConfigurationClass.getDeclaredMethod("newDocumentBuilder");
                isLegacy = true;
            }
            method.setAccessible(true);
            DocumentBuilder builder;
            InputStream inputStream;
            if (!isLegacy) {
                builder = (DocumentBuilder) method.invoke(config, true);
                inputStream = config.getConfigurationSource().resetInputStream().getInputStream();
            } else {
                builder = (DocumentBuilder) method.invoke(config);
                inputStream = config.getClass().getClassLoader().getResourceAsStream("log4j2.xml");
            }
            Document document = builder.parse(inputStream);
            inputStream.close();
            Element element = document.getDocumentElement();
            return getChild(element, "Appenders");
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Class<?>> getConverters(Configuration config) {
        try {
            config.getPluginPackages().add("io.github.rothes.protocolstringreplacer.console");
        } catch (NoSuchMethodError ignored) {}

        try {
            Field field;
            PatternParser patternParser = PatternLayout.createPatternParser(config);
            field = PatternParser.class.getDeclaredField("converterRules");
            field.setAccessible(true);
            return (Map<String, Class<?>>) field.get(patternParser);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private Node getChild(@Nonnull Node node, @Nonnull String childName) {
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals(childName)) {
                return childNode;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Class<?> getPluginClass(Configuration config, String name) {
        Map<String, ?> map;
        try {
            map = ((AbstractConfiguration) config).getPluginManager().getPlugins();
        } catch (NoClassDefFoundError e) {
            try {
                Class<?> clazz = Class.forName("org.apache.logging.log4j.core.config.BaseConfiguration");
                Field pluginManagerField = clazz.getDeclaredField("pluginManager");
                pluginManagerField.setAccessible(true);
                Object pluginManager = pluginManagerField.get(config);
                map = (Map<String, ?>) pluginManager.getClass().getDeclaredMethod("getPlugins").invoke(pluginManager);
            } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        Object o = map.get(name);
        try {
            return o == null ? null : (Class<?>) o.getClass().getDeclaredMethod("getPluginClass").invoke(o);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void fixJndi(Configuration config, boolean restore) {
        try {
            Field field;
            try {
                field = config.getClass().getSuperclass().getDeclaredField("subst");
            } catch (NoSuchFieldException e) {
                field = config.getClass().getSuperclass().getDeclaredField("runtimeStrSubstitutor");
            }
            field.setAccessible(true);
            StrSubstitutor substitutor = (StrSubstitutor) field.get(config);
            Interpolator interpolator = (Interpolator) substitutor.getVariableResolver();
            if (interpolator == null)
                return;
            try {
                field = interpolator.getClass().getDeclaredField("strLookupMap");
            } catch (NoSuchFieldException e) {
                field = interpolator.getClass().getDeclaredField("lookups");
            }
            field.setAccessible(true);
            Map<String, StrLookup> pluginsMap = (Map<String, StrLookup>) field.get(interpolator);
            if (restore) {
                if (oriJndiLkup instanceof StrLookup) {
                    pluginsMap.put("jndi", (StrLookup) oriJndiLkup);
                } else {
                    pluginsMap.remove("jndi");
                }
            } else {
                oriJndiLkup = pluginsMap.get("jndi");
                pluginsMap.put("jndi", new PsrJndiLookup());
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
