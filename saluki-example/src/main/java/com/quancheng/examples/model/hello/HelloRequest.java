package com.quancheng.examples.model.hello;

import com.quancheng.saluki.serializer.ProtobufAttribute;
import com.quancheng.saluki.serializer.ProtobufEntity;

@ProtobufEntity(com.quancheng.examples.model.Hello.HelloRequest.class)
public class HelloRequest { 

    @ProtobufAttribute
    private String name ;

    public String getName () {
        return this.name ;
    }

    public void setName (String name ) {
        this.name  = name ;
    }
} 
