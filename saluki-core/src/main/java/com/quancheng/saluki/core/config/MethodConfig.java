package com.quancheng.saluki.core.config;

import java.io.Serializable;

public class MethodConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    private String            name;

    private int               reties;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getReties() {
        return reties;
    }

    public void setReties(int reties) {
        this.reties = reties;
    }

}
