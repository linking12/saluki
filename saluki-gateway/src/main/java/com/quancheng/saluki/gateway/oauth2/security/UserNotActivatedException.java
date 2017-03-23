package com.quancheng.saluki.gateway.oauth2.security;

import org.springframework.security.core.AuthenticationException;

public class UserNotActivatedException extends AuthenticationException {

    private static final long serialVersionUID = 1L;

    public UserNotActivatedException(String msg, Throwable t){
        super(msg, t);
    }

    public UserNotActivatedException(String msg){
        super(msg);
    }
}
