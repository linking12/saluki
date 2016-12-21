/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul;

import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.registry.consul.model.ThrallRoleType;

/**
 * @author shimingliu 2016年12月20日 下午9:14:15
 * @version ThrallURLUtils.java, v 0.0.1 2016年12月20日 下午9:14:15 shimingliu
 */
public class GrpcURLUtils {

    private GrpcURLUtils(){

    }

    /**** help method *****/
    public static String toServiceName(String group) {
        return Constants.CONSUL_SERVICE_PRE + group;
    }

    private static String toServicePath(GrpcURL url) {
        String name = url.getServiceInterface();
        String group = url.getGroup();
        return group + Constants.PATH_SEPARATOR + GrpcURL.encode(name);
    }

    public static String toCategoryPathNotIncludeVersion(GrpcURL url, ThrallRoleType roleType) {
        switch (roleType) {
            case CONSUMER:
                return toServicePath(url) + Constants.PATH_SEPARATOR + Constants.CONSUMERS_CATEGORY;
            case PROVIDER:
                return toServicePath(url) + Constants.PATH_SEPARATOR + Constants.PROVIDERS_CATEGORY;
            default:
                throw new java.lang.IllegalArgumentException("there is no role type");
        }

    }

    public static String toCategoryPathIncludeVersion(GrpcURL url, ThrallRoleType roleType) {
        switch (roleType) {
            case CONSUMER:
                return toServicePath(url) + Constants.PATH_SEPARATOR + url.getVersion() + Constants.PATH_SEPARATOR
                       + Constants.CONSUMERS_CATEGORY;
            case PROVIDER:
                return toServicePath(url) + Constants.PATH_SEPARATOR + url.getVersion() + Constants.PATH_SEPARATOR
                       + Constants.PROVIDERS_CATEGORY;
            default:
                throw new java.lang.IllegalArgumentException("there is no role type");
        }

    }

    public static String healthServicePath(GrpcURL url, ThrallRoleType roleType) {
        return toCategoryPathNotIncludeVersion(url, roleType) + Constants.PATH_SEPARATOR
               + GrpcURL.encode(url.toFullString());
    }

    public static String ephemralNodePath(GrpcURL url, ThrallRoleType roleType) {
        return Constants.CONSUL_SERVICE_PRE + toCategoryPathIncludeVersion(url, roleType) + Constants.PATH_SEPARATOR
               + url.getAddress();
    }

}
