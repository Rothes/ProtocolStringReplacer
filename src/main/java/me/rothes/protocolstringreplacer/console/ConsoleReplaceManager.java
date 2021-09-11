package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.replacer.ListenType;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.user.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.ReusableMessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.bukkit.Bukkit;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.function.BiPredicate;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class ConsoleReplaceManager {

    private static final BiPredicate<ReplacerConfig, User> filter = (replacerConfig, user) -> replacerConfig.getListenTypeList().contains(ListenType.CONSOLE);
    private final ProtocolStringReplacer plugin;

    public static BiPredicate<ReplacerConfig, User> getFilter() {
        return filter;
    }

    public ConsoleReplaceManager(ProtocolStringReplacer plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter(){
            @Override
            public String formatMessage(LogRecord record) {
                return ProtocolStringReplacer.getInstance().getReplacerManager().
                        getReplacedString(super.formatMessage(record), plugin.getUserManager().getConsoleUser(), filter);
            }
        });

        if (plugin.getServerMajorVersion() >= 12) {
            try {
                Field field = AbstractLogger.class.getDeclaredField("messageFactory");
                field.setAccessible(true);
                PSRMessageFactory psrMessageFactory = new PSRMessageFactory();
                field.set(LogManager.getRootLogger(), psrMessageFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            System.setOut(new PrintStream(System.out) {
                @Override
                public void println(String line) {
                    super.println(plugin.getReplacerManager().getReplacedString(line, plugin.getUserManager().getConsoleUser(), filter));
                }
            });
        }

    }

    public void disable() {
        Bukkit.getServer().getLogger().getParent().getHandlers()[0].setFormatter(new SimpleFormatter());
        if (plugin.getServerMajorVersion() >= 12) {
            try {
                Field field = AbstractLogger.class.getDeclaredField("messageFactory");
                field.setAccessible(true);
                ReusableMessageFactory reusableMessageFactory = new ReusableMessageFactory();
                field.set(LogManager.getRootLogger(), reusableMessageFactory);
                field.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        } else {
            System.setOut(new PrintStream(System.out));
        }
    }

}
