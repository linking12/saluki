package com.quancheng.saluki.boot.jaket.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class MethodDefinition {

    private String name;
    private String[] parameterTypes;
    private String returnType;
    private List<TypeDefinition> parameters;

    public String getName() {
        return name;
    }

    public List<TypeDefinition> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<TypeDefinition>();
        }
        return parameters;
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParameters(List<TypeDefinition> parameters) {
        this.parameters = parameters;
    }

    public void setParameterTypes(String[] parameterTypes) {
        this.parameterTypes = parameterTypes;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return "MethodDefinition [name=" + name + ", parameterTypes=" + Arrays.toString(parameterTypes)
                + ", returnType=" + returnType + "]";
    }

}
