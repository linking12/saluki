package com.quancheng.saluki.core.config;

import java.io.Serializable;

public class AbstractConfig implements Serializable {

    private static final long serialVersionUID = 5736580957909744603L;

    protected String          id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
