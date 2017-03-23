/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2.support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author shimingliu 2017年3月23日 上午11:26:10
 * @version Oauth2UserService.java, v 0.0.1 2017年3月23日 上午11:26:10 shimingliu
 */
@Service
public class Oauth2UserDetailsService implements UserDetailsService {

    @Autowired
    private Oauth2UserStore oauth2UserStore;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return oauth2UserStore.loadUserByUsername(username);
    }

    public String loadUsernameByToken(String token) throws UsernameNotFoundException {
        return oauth2UserStore.loadUsernameByToken(token);
    }

}
