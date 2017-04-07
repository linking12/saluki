package com.quancheng.saluki.gateway.oauth2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.quancheng.saluki.gateway.oauth2.entity.RoleEntity;
import com.quancheng.saluki.gateway.oauth2.entity.UserEntity;
import com.quancheng.saluki.gateway.oauth2.entity.UserRoleXrefEntity;
import com.quancheng.saluki.gateway.oauth2.repository.RoleRepository;
import com.quancheng.saluki.gateway.oauth2.repository.UserRepository;
import com.quancheng.saluki.gateway.oauth2.repository.UserRoleXrefRepository;

@Configuration
@Profile("default-user-and-roles_route")
public class DefaultUserAndRolesConfiguration implements InitializingBean {

    private static final String    DEFAULT_ADMIN_USERNAME = "admin";
    private static final String    DEFAULT_ADMIN_PASSWORD = "admin888";

    private static final String    DEFAULT_USER_USERNAME  = "user";
    private static final String    DEFAULT_USER_PASSWORD  = "user888";

    private static final String[]  DEFAULT_ROLES          = { "ADMIN", "USER" };

    @Autowired
    private UserRepository         userRepository;

    @Autowired
    private RoleRepository         roleRepository;

    @Autowired
    private UserRoleXrefRepository userRoleXrefRepository;

    @Autowired
    private PasswordEncoder        passwordEncoder;

    @Transactional
    @Override
    public void afterPropertiesSet() throws Exception {
        List<RoleEntity> defaultRoleEntities = new ArrayList<>();
        Arrays.stream(DEFAULT_ROLES).forEach(role -> defaultRoleEntities.add(roleRepository.findOneByName(role).orElseGet(() -> roleRepository.save(RoleEntity.builder().name(role).build()))));

        UserEntity defaultAdminUserEntity = userRepository.findOneByUsername(DEFAULT_ADMIN_USERNAME).orElseGet(() -> userRepository.save(UserEntity.builder().username(DEFAULT_ADMIN_USERNAME).password(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD)).build()));

        defaultRoleEntities.stream().forEach(roleEntity -> userRoleXrefRepository.save(UserRoleXrefEntity.builder().user(defaultAdminUserEntity).role(roleEntity).build()));

        userRepository.findOneByUsername(DEFAULT_USER_USERNAME).orElseGet(() -> {
            UserEntity userEntity = UserEntity.builder().username(DEFAULT_USER_USERNAME).password(passwordEncoder.encode(DEFAULT_USER_PASSWORD)).build();

            roleRepository.findOneByName("USER").ifPresent(roleEntity -> userEntity.setRoles(Collections.singleton(UserRoleXrefEntity.builder().user(userEntity).role(roleEntity).build())));

            return userRepository.save(userEntity);
        });
    }
}
