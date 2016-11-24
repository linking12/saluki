package com.quancheng.saluki.monitor.service.support.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.Expose;

/**
 * Created by huangsheng.hs on 2015/1/27.
 */
public class TypeDefinition {

    @Expose(deserialize = false)
    private String                      id;

    private String                      type;

    @Expose(deserialize = false)
    private List<TypeDefinition>        items;

    @Expose(deserialize = false)
    private List<String>                enums;

    @Expose(deserialize = false)
    private String                      $ref;

    private Map<String, TypeDefinition> properties;

    public TypeDefinition(String type){
        this.type = type;
    }

    public String get$ref() {
        return $ref;
    }

    public List<String> getEnums() {
        if (enums == null) {
            enums = new ArrayList<String>();
        }
        return enums;
    }

    public String getId() {
        return id;
    }

    public List<TypeDefinition> getItems() {
        if (items == null) {
            items = new ArrayList<TypeDefinition>();
        }
        return items;
    }

    public Map<String, TypeDefinition> getProperties() {
        if (properties == null) {
            properties = new HashMap<String, TypeDefinition>();
        }
        return properties;
    }

    public String getType() {
        return type;
    }

    public void set$ref(String $ref) {
        this.$ref = $ref;
    }

    public void setEnums(List<String> enums) {
        this.enums = enums;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setItems(List<TypeDefinition> items) {
        this.items = items;
    }

    public void setProperties(Map<String, TypeDefinition> properties) {
        this.properties = properties;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "TypeDefinition [id=" + id + ", type=" + type + ", properties=" + properties + ", $ref=" + $ref + "]";
    }

}
