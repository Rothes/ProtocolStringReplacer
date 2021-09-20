package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import me.rothes.protocolstringreplacer.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.AbstractConfiguration;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.bukkit.Bukkit;

import java.lang.reflect.Field;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ConsoleReplaceManager {

    private static final BiPredicate<ReplacerConfig, User> filter = (replacerConfig, user) -> replacerConfig.getListenTypeList().contains(ListenType.CONSOLE);
    private final ProtocolStringReplacer plugin;
    private PSRFilter psrFilter;

    public static BiPredicate<ReplacerConfig, User> getFilter() {
        return filter;
    }

    public PSRFilter getPsrFilter() {
        return psrFilter;
    }

    public ConsoleReplaceManager(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        // This is for plugin Logger and server things.
        Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter(){
            @Override
            public String formatMessage(LogRecord record) {
                SimpleTextContainer container = new SimpleTextContainer(super.formatMessage(record));
                container.createTexts(container);
                ReplacerManager replacerManager = plugin.getReplacerManager();
                List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(plugin.getUserManager().getConsoleUser(), filter);
                replacerManager.replaceContainerTexts(container, replacers);
                return container.getResult();
            }
        });
        Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFilter(record -> {
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

    public void disable() {
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
