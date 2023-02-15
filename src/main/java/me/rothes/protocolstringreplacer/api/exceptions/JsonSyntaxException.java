package me.rothes.protocolstringreplacer.api.exceptions;

public class JsonSyntaxException extends RuntimeException {

    public JsonSyntaxException(String msg, Throwable throwable) {
        super(msg, throwable);
    }

}
