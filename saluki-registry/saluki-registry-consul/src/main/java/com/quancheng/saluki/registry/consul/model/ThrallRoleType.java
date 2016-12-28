/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.model;

/**
 * @author shimingliu 2016年12月16日 上午10:49:33
 * @version EphemralType.java, v 0.0.1 2016年12月16日 上午10:49:33 shimingliu
 */
public enum ThrallRoleType {
                          CONSUMER(0),

                          PROVIDER(1);

    private final int value;

    private ThrallRoleType(int value){
        this.value = value;
    }

    public final int getNumber() {
        return value;
    }

    public static ThrallRoleType forNumber(Integer value) {
        switch (value) {
            case 0:
                return CONSUMER;
            case 1:
                return CONSUMER;
            default:
                return null;
        }
    }
}
