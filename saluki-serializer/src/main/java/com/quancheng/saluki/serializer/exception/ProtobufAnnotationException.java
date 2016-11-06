
package com.quancheng.saluki.serializer.exception;

public class ProtobufAnnotationException extends ProtobufException {

    private static final long serialVersionUID = 1L;

    public ProtobufAnnotationException(Exception exception){
        super(exception);
    }

    public ProtobufAnnotationException(String string){
        super(string);
    }

    public ProtobufAnnotationException(String string, Exception exception){
        super(string, exception);
    }
}
