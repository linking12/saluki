package com.quancheng.saluki.boot.jaket.model;

import java.util.List;

/**
 * @author bw on 11/25/15.
 */
@SuppressWarnings("rawtypes")
public class GenericInvokeMetadata {

    private String signature;
    private List   parameterTypes;
    private Object returnType;

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public List getParameterTypes() {
        return parameterTypes;
    }

    public void setParameterTypes(List<Object> parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public Object getReturnType() {
        return returnType;
    }

    public void setReturnType(Object returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "GenericInvokeMetadata{" + "signature='" + signature + '\'' + ", parameterTypes=" + parameterTypes
               + ", returnType=" + returnType + '}';
    }
}
