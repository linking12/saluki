package com.quancheng.examples.model.hello;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;

@ProtobufEntity(com.quancheng.examples.model.Hello.HelloReply.class)
public class HelloReply { 

    @ProtobufAttribute
    private String message ;

    public String getMessage () {
        return this.message ;
    }

    public void setMessage (String message ) {
        this.message  = message ;
    }
} 
