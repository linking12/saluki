package com.quancheng.saluki.gateway.oauth2.service;

import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.quancheng.saluki.gateway.oauth2.repository.UserRepository;

@Service
public class DatabaseUserDetailService implements UserDetailsService {

    private static final String ROLE_PREFIX = "ROLE_";

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findOneByUsername(username).map(userEntity ->
                new User(userEntity.getUsername(),
                        userEntity.getPassword(),
                        userEntity.getRoles().stream().map(userRoleXRef ->
                                new SimpleGrantedAuthority(prefixRoleName(userRoleXRef.getRole().getName())))
                                .collect(Collectors.toList())))
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + " was not found in the database"));
    }

    private String prefixRoleName(String roleName){
        if (!StringUtils.isEmpty(roleName) && !roleName.startsWith(ROLE_PREFIX)){
            return ROLE_PREFIX + roleName;
        }
        return roleName;
    }
}
