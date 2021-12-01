package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import me.rothes.protocolstringreplacer.api.user.PsrUser;
import me.rothes.protocolstringreplacer.replacer.ReplacerConfig;
import me.rothes.protocolstringreplacer.replacer.ReplacerManager;
import me.rothes.protocolstringreplacer.replacer.containers.SimpleTextContainer;
import org.apache.logging.log4j.message.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Objects;

/**
 * This is a fork of SimpleMessage, only to make PSR hook into it.
 */
public class PsrMessage implements Message, CharSequence{

    private static ProtocolStringReplacer plugin;

    private String message;
    private transient CharSequence charSequence;

    /**
     * Constructor that includes the message.
     * @param message The String message.
     */
    public PsrMessage(final String message) {
        if (plugin.hasStarted()) {
            SimpleTextContainer container = new SimpleTextContainer(message);
            container.createTexts(container);
            ReplacerManager replacerManager = plugin.getReplacerManager();
            PsrUser consoleUser = plugin.getUserManager().getConsoleUser();
            List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(consoleUser,
                    ConsoleReplaceManager.getFilter());
            replacerManager.replaceContainerTexts(container, replacers);
            if (plugin.getConfigManager().consolePlaceholder) {
                replacerManager.setPapi(consoleUser, container.getTexts());
            }
            String result = container.getResult();
            this.message = result;
            this.charSequence = result;
        } else {
            this.message = message;
            this.charSequence = message;
        }
    }

    /**
     * Constructor that includes the message.
     * @param charSequence The CharSequence message.
     */
    public PsrMessage(final CharSequence charSequence) {
        if (plugin.hasStarted()) {
            SimpleTextContainer container = new SimpleTextContainer(charSequence.toString());
            container.createTexts(container);
            ReplacerManager replacerManager = plugin.getReplacerManager();
            PsrUser consoleUser = plugin.getUserManager().getConsoleUser();
            List<ReplacerConfig> replacers = replacerManager.getAcceptedReplacers(consoleUser,
                    ConsoleReplaceManager.getFilter());
            replacerManager.replaceContainerTexts(container, replacers);
            if (plugin.getConfigManager().consolePlaceholder) {
                replacerManager.setPapi(consoleUser, container.getTexts());
            }
            // this.message = String.valueOf(charSequence); // postponed until getFormattedMessage
            this.charSequence = container.getResult();
        } else {
            this.charSequence = charSequence;
        }
    }

    public static void initialize(ProtocolStringReplacer plugin) {
        PsrMessage.plugin = plugin;
    }

    /**
     * Returns the message.
     * @return the message.
     */
    @Override
    public String getFormattedMessage() {
        return message = message == null ? String.valueOf(charSequence) : message ;
    }

    /**
     * Returns the message.
     * @return the message.
     */
    @Override
    public String getFormat() {
        return message;
    }

    /**
     * Returns null since there are no parameters.
     * @return null.
     */
    @Override
    public Object[] getParameters() {
        return null;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final PsrMessage that = (PsrMessage) o;

        return Objects.equals(charSequence, that.charSequence);
    }

    @Override
    public int hashCode() {
        return charSequence != null ? charSequence.hashCode() : 0;
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    /**
     * Always returns null.
     *
     * @return null
     */
    @Override
    public Throwable getThrowable() {
        return null;
    }


    // CharSequence impl

    @Override
    public int length() {
        return charSequence == null ? 0 : charSequence.length();
    }

    @Override
    public char charAt(final int index) {
        return charSequence.charAt(index);
    }

    @Override
    public CharSequence subSequence(final int start, final int end) {
        return charSequence.subSequence(start, end);
    }


    private void writeObject(final ObjectOutputStream out) throws IOException {
        getFormattedMessage(); // initialize the message:String field
        out.defaultWriteObject();
    }

    private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        charSequence = message;
    }
}
