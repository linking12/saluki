package com.quancheng.saluki.gateway.oauth2;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import com.quancheng.saluki.gateway.oauth2.entity.GrantTypeEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ScopeEntity;
import com.quancheng.saluki.gateway.oauth2.repository.GrantTypeRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ScopeRepository;
import com.quancheng.saluki.gateway.oauth2.service.OAuth2DatabaseClientDetailsService;

@Configuration
@Profile("default-user-and-roles")
public class DefaultClientDetailsConfig implements InitializingBean {

    private static final Logger                logger              = LoggerFactory.getLogger(DefaultClientDetailsConfig.class);

    private static final String[]              DEFAULT_GRANT_TYPES = { "authorization_code", "refresh_token",
                                                                       "password" };

    private static final String[]              DEFAULT_SCOPES      = { "read", "write", "trust" };

    @Autowired
    private GrantTypeRepository                grantTypeRepository;

    @Autowired
    private ScopeRepository                    scopeRepository;

    @Autowired
    private OAuth2DatabaseClientDetailsService oAuth2DatabaseClientDetailsService;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (grantTypeRepository.count() == 0) {
            grantTypeRepository.save(Arrays.stream(DEFAULT_GRANT_TYPES)//
                                           .map(grantType -> GrantTypeEntity.builder().value(grantType).build())//
                                           .collect(Collectors.toList()));
        }
        if (scopeRepository.count() == 0) {
            scopeRepository.save(Arrays.stream(DEFAULT_SCOPES)//
                                       .map(scope -> ScopeEntity.builder().value(scope).build())//
                                       .collect(Collectors.toList()));
        }
        BaseClientDetails clientDetails = new BaseClientDetails("test-client-id", null, "read,write,trust",
                                                                "authorization_code,refresh_token", null);
        clientDetails.setClientSecret("test-client-id-secret-123");
        clientDetails.setRegisteredRedirectUri(Collections.emptySet());

        try {
            oAuth2DatabaseClientDetailsService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }

        clientDetails = new BaseClientDetails("test-res-client", null, null, null, null);
        clientDetails.setClientSecret("test-res-client-secret-123");
        clientDetails.setRegisteredRedirectUri(Collections.singleton("http://test.com"));

        try {
            oAuth2DatabaseClientDetailsService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }

        clientDetails = new BaseClientDetails("test_password_client", null, "trust", "password", null);
        clientDetails.setClientSecret("1234567");
        clientDetails.setRegisteredRedirectUri(Collections.emptySet());

        try {
            oAuth2DatabaseClientDetailsService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }

    }
}
