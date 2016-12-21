package com.quancheng.saluki.boot.jaket.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class ServiceDefinition {

    private String canonicalName;
    private String codeSource;
    private List<MethodDefinition> methods;
    private List<TypeDefinition> types;

    public String getCanonicalName() {
        return canonicalName;
    }

    public String getCodeSource() {
        return codeSource;
    }

    public List<MethodDefinition> getMethods() {
        if (methods == null) {
            methods = new ArrayList<MethodDefinition>();
        }
        return methods;
    }

    public List<TypeDefinition> getTypes() {
        if (types == null) {
            types = new ArrayList<TypeDefinition>();
        }
        return types;
    }

    public String getUniqueId() {
        return canonicalName + "@" + codeSource;
    }

    public void setCanonicalName(String canonicalName) {
        this.canonicalName = canonicalName;
    }

    public void setCodeSource(String codeSource) {
        this.codeSource = codeSource;
    }

    public void setMethods(List<MethodDefinition> methods) {
        this.methods = methods;
    }

    public void setTypes(List<TypeDefinition> types) {
        this.types = types;
    }

    @Override
    public String toString() {
        return "ServiceDefinition [canonicalName=" + canonicalName + ", codeSource=" + codeSource + ", methods="
                + methods + "]";
    }

}
