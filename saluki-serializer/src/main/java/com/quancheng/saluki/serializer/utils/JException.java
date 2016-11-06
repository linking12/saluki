package com.quancheng.saluki.serializer.utils;

public class JException extends Exception {

    private static final long serialVersionUID = -3979425625563840307L;

    public JException(Exception exception){
        super(exception);
    }

    public JException(String string){
        super(string);
    }

    public JException(String string, Exception exception){
        super(string, exception);
    }
}
