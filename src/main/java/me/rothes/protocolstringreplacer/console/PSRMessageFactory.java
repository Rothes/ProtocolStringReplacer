package me.rothes.protocolstringreplacer.console;

import org.apache.logging.log4j.message.AbstractMessageFactory;
import org.apache.logging.log4j.message.Message;

public class PSRMessageFactory extends AbstractMessageFactory {

    @Override
    public Message newMessage(String message) {
        return new PSRMessage(message);
    }

    @Override
    public Message newMessage(CharSequence message) {
        return new PSRMessage(message);
    }

    @Override
    public Message newMessage(String message, Object... params) {
        return new PSRMessage(message);
    }

}
