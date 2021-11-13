package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.User;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import net.minecrell.terminalconsole.util.LoggerNamePatternSelector;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusLogger;
import org.bukkit.Bukkit;
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
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ConsoleReplaceManager {

    private static final BiPredicate<ReplacerConfig, User> filter = (replacerConfig, user) -> replacerConfig.getListenTypeList().contains(ListenType.CONSOLE);
    private ProtocolStringReplacer plugin;
    private PSRFilter psrFilter;

    public ConsoleReplaceManager(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
    }

    public static BiPredicate<ReplacerConfig, User> getFilter() {
        return filter;
    }

    public void initialize() {
        if (plugin.getServerMajorVersion() >= 12) {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();
            // Get the default xml configuration from server jar.
            Node appenders = getAppendersNode(config);

            // Add PSR converter.
            getConverters(config).put("PSRFormatting", PSRLogEventPatternConverter.class);

            NodeList childNodes = appenders.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeName().equals("#text")) {
                    continue;
                }
                String name = item.getAttributes().getNamedItem("name").getNodeValue();
                boolean removeAnsi = item.getNodeName().equals("RollingRandomAccessFile") || item.getNodeName().equals("ServerGuiConsole");
                Node appenderNode = getChild(item, "PatternLayout");
                if (appenderNode != null) {
                    setAppender(config, appenderNode, name, removeAnsi, false);
                }
            }

            // For some version we still need to do this.
            psrFilter = new PSRFilter(plugin);
            config.addFilter(psrFilter);

        } else {
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
                    User consoleUser = plugin.getUserManager().getConsoleUser();
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
                PSRMessageFactory psrMessageFactory = new PSRMessageFactory();
                field.set(LogManager.getRootLogger(), psrMessageFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
            psrFilter = new PSRFilter(plugin);
            ((Logger) LogManager.getRootLogger()).addFilter(psrFilter);
        }
    }

    public void disable() {
        if (plugin.getServerMajorVersion() >= 12) {
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            Configuration config = context.getConfiguration();
            // Get the default xml configuration from server jar.
            Node appenders = getAppendersNode(config);

            // Remove PSR converter.
            getConverters(config).remove("PSRFormatting");

            NodeList childNodes = appenders.getChildNodes();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node item = childNodes.item(i);
                if (item.getNodeName().equals("#text")) {
                    continue;
                }
                String name = item.getAttributes().getNamedItem("name").getNodeValue();
                boolean removeAnsi = item.getNodeName().equals("RollingRandomAccessFile") || item.getNodeName().equals("ServerGuiConsole");
                Node appenderNode = getChild(item, "PatternLayout");
                if (appenderNode != null) {
                    setAppender(config, appenderNode, name, removeAnsi, true);
                }
            }

            config.removeFilter(psrFilter);

        } else {
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter());
            Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFilter(null);
            try {
                Field field = AbstractLogger.class.getDeclaredField("messageFactory");
                field.setAccessible(true);
                ReusableMessageFactory reusableMessageFactory = new ReusableMessageFactory();
                field.set(LogManager.getRootLogger(), reusableMessageFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            // Try to remove the filter we added.
            Logger rootLogger = (Logger) LogManager.getRootLogger();
            try {
                Field privateConfig = Logger.class.getDeclaredField("privateConfig");
                privateConfig.setAccessible(true);
                Object o = privateConfig.get(rootLogger);
                privateConfig.setAccessible(false);
                Field configField = o.getClass().getDeclaredField("config");
                AbstractConfiguration config = (AbstractConfiguration) configField.get(o);
                config.getLoggerConfig(rootLogger.getName()).removeFilter(psrFilter);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setAppender(Configuration config, Node appenderNode, String appenderName, boolean removeAnsi, boolean restore) {
        try {
            Field field;
            field = AbstractAppender.class.getDeclaredField("layout");
            field.setAccessible(true);
            PatternLayout layout = (PatternLayout) field.get(config.getAppender(appenderName));
            Node selectorNode = getChild(appenderNode, "LoggerNamePatternSelector");
            if (selectorNode != null) {
                PatternParser parser = PatternLayout.createPatternParser(config);
                String defaultPattern = selectorNode.getAttributes().getNamedItem("defaultPattern").getNodeValue();

                field = PatternLayout.class.getDeclaredField("patternSelector");
                field.setAccessible(true);
                LoggerNamePatternSelector selector = (LoggerNamePatternSelector) field.get(layout);

                field = LoggerNamePatternSelector.class.getDeclaredField("defaultFormatters");
                field.setAccessible(true);
                field.set(selector, parser.parse(restore ? defaultPattern : ("%PSRFormatting{" + defaultPattern + "}" + (removeAnsi ? "{removeAnsi}" : ""))).toArray(new PatternFormatter[0]));

                Node patternMatch = getChild(selectorNode, "PatternMatch");
                String pattern = patternMatch.getAttributes().getNamedItem("pattern").getNodeValue();
                field = LoggerNamePatternSelector.class.getDeclaredField("formatters");
                field.setAccessible(true);
                List<?> formatters = (List<?>) field.get(selector);
                for (Object formatter : formatters) {
                    field = formatter.getClass().getDeclaredField("formatters");
                    field.setAccessible(true);
                    field.set(formatter, parser.parse(restore ? pattern : ("%PSRFormatting{" + pattern + "}" + (removeAnsi ? "{removeAnsi}" : ""))).toArray(new PatternFormatter[0]));
                }

            } else {
                field = PatternLayout.class.getDeclaredField("conversionPattern");
                field.setAccessible(true);
                String defaultPattern = (String) field.get(layout);
                String pattern = restore ? appenderNode.getAttributes().getNamedItem("pattern").getNodeValue()
                        : ("%PSRFormatting{" + defaultPattern + "}" + (removeAnsi ? "{removeAnsi}" : ""));
                field.set(layout, pattern);
                field = PatternLayout.class.getDeclaredField("eventSerializer");
                field.setAccessible(true);
                field.set(layout, PatternLayout.newSerializerBuilder().setConfiguration(config).setPattern(pattern).setDefaultPattern(pattern).build());
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
            } catch (ClassNotFoundException e) {
                return null;
            }
            Method method = xmlConfigurationClass.getDeclaredMethod("newDocumentBuilder", boolean.class);
            method.setAccessible(true);
            DocumentBuilder builder = (DocumentBuilder) method.invoke(config, true);
            InputStream inputStream = config.getConfigurationSource().resetInputStream().getInputStream();
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
        config.getPluginPackages().add("me.rothes.protocolstringreplacer.console");
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
    private Node getChild(@Nonnull Element element, @Nonnull String childName) {
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if (childNode.getNodeName().equals(childName)) {
                return childNode;
            }
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
    }/*

    @Nullable
    private String getAttribute(@Nonnull Node node, @Nonnull String key) {
        NamedNodeMap attributes = node.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            attributes.it
        }
    }*/

}
