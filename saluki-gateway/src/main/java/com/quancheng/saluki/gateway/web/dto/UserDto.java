/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.web.dto;

import java.io.Serializable;

import com.quancheng.saluki.gateway.oauth2.security.Authorities;

/**
 * @author shimingliu 2017年3月30日 上午10:02:37
 * @version UserDto.java, v 0.0.1 2017年3月30日 上午10:02:37 shimingliu
 */
public class UserDto implements Serializable {

    private static final long serialVersionUID = 110939194399969390L;

    private String            userName;

    private String            email;

    private String            password;

    private String            activated;

    private Authorities       authority;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getActivated() {
        return activated;
    }

    public void setActivated(String activated) {
        this.activated = activated;
    }

    public Authorities getAuthority() {
        return authority;
    }

    public void setAuthority(Authorities authority) {
        this.authority = authority;
    }

}
