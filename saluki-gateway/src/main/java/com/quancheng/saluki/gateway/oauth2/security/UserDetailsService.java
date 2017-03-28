package com.quancheng.saluki.gateway.oauth2.security;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Maps;
import com.quancheng.saluki.gateway.oauth2.support.Authority;
import com.quancheng.saluki.gateway.oauth2.support.User;
import com.quancheng.saluki.gateway.oauth2.support.UserRepository;

@Component("userDetailsService")
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private final Logger   log = LoggerFactory.getLogger(UserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    private MessageDigest  messageDigest;

    @PostConstruct
    public void init() {
        try {
            messageDigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
        }
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(final String login) {

        log.debug("Authenticating {}", login);
        String lowercaseLogin = login.toLowerCase();

        User userFromDatabase;
        if (lowercaseLogin.contains("@")) {
            userFromDatabase = userRepository.findByEmail(lowercaseLogin);
        } else {
            userFromDatabase = userRepository.findByUsernameCaseInsensitive(lowercaseLogin);
        }

        if (userFromDatabase == null) {
            throw new UsernameNotFoundException("User " + lowercaseLogin + " was not found in the database");
        } else if (!userFromDatabase.isActivated()) {
            throw new UserNotActivatedException("User " + lowercaseLogin + " is not activated");
        }

        Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (Authority authority : userFromDatabase.getAuthorities()) {
            GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority.getName());
            grantedAuthorities.add(grantedAuthority);
        }

        return new org.springframework.security.core.userdetails.User(userFromDatabase.getUsername(),
                                                                      userFromDatabase.getPassword(),
                                                                      grantedAuthorities);

    }

    public Map<String, Object> loadUsernameByToken(String token) {
        if (token == null) {
            return null;
        }
        try {
            byte[] bytes = messageDigest.digest(token.getBytes("UTF-8"));
            String realToken = String.format("%032x", new BigInteger(1, bytes));
            User userFromDatabase = userRepository.findByToken(realToken);
            List<String> authorities = userRepository.findUserAuthority(userFromDatabase.getUsername());
            Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();
            for (String authority : authorities) {
                GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(authority);
                grantedAuthorities.add(grantedAuthority);
            }
            Map<String, Object> userParam = Maps.newHashMap();
            userParam.put("user",
                          new org.springframework.security.core.userdetails.User(userFromDatabase.getUsername(),
                                                                                 userFromDatabase.getPassword(),
                                                                                 grantedAuthorities));
            userParam.put("intervalInMills", userFromDatabase.getIntervalInMills());
            userParam.put("limit", userFromDatabase.getLimit());
            return userParam;
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding not available.  Fatal (should be in the JDK).");
        }

    }

}
