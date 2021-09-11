package me.rothes.protocolstringreplacer.console;

import me.rothes.protocolstringreplacer.ProtocolStringReplacer;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.StringBuilderFormattable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

/**
 * This is a fork of SimpleMessage, only to make PSR hook into it.
 */
public class PSRMessage implements Message, StringBuilderFormattable, CharSequence{
    private static final long serialVersionUID = -8398002534962715992L;
    private static ProtocolStringReplacer plugin;

    private String message;
    private transient CharSequence charSequence;

    /**
     * Basic constructor.
     */
    public PSRMessage() {
        this(null);
    }

    /**
     * Constructor that includes the message.
     * @param message The String message.
     */
    public PSRMessage(final String message) {
        String replaced = plugin.getReplacerManager().getReplacedString(message, plugin.getUserManager().getConsoleUser(),
                ConsoleReplaceManager.getFilter());
        this.message = replaced;
        this.charSequence = replaced;
    }

    /**
     * Constructor that includes the message.
     * @param charSequence The CharSequence message.
     */
    public PSRMessage(final CharSequence charSequence) {
        // this.message = String.valueOf(charSequence); // postponed until getFormattedMessage
        this.charSequence = plugin.getReplacerManager().getReplacedString(charSequence.toString(), plugin.getUserManager().getConsoleUser(),
                ConsoleReplaceManager.getFilter());
    }

    public static void initialize(ProtocolStringReplacer plugin) {
        PSRMessage.plugin = plugin;
    }

    /**
     * Returns the message.
     * @return the message.
     */
    @Override
    public String getFormattedMessage() {
        return message = message == null ? String.valueOf(charSequence) : message ;
    }

    @Override
    public void formatTo(final StringBuilder buffer) {
        buffer.append(message != null ? message : charSequence);
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

        final PSRMessage that = (PSRMessage) o;

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
