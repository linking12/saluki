package com.quancheng.saluki.core.grpc.service;

import java.io.PrintWriter;
import java.io.StringWriter;

public class GenericException extends RuntimeException {

    private static final long serialVersionUID = -1182299763306599962L;

    private String            exceptionClass;

    private String            exceptionMessage;

    public GenericException(){
    }

    public GenericException(String exceptionClass, String exceptionMessage){
        super(exceptionMessage);
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
    }

    public GenericException(Throwable cause){
        super(toString(cause));
        this.exceptionClass = cause.getClass().getName();
        this.exceptionMessage = cause.getMessage();
    }

    public String getExceptionClass() {
        return exceptionClass;
    }

    public void setExceptionClass(String exceptionClass) {
        this.exceptionClass = exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    private static String toString(Throwable e) {
        StringWriter w = new StringWriter();
        PrintWriter p = new PrintWriter(w);
        p.print(e.getClass().getName());
        if (e.getMessage() != null) {
            p.print(": " + e.getMessage());
        }
        p.println();
        try {
            e.printStackTrace(p);
            return w.toString();
        } finally {
            p.close();
        }
    }

}
