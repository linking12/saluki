package com.quancheng.saluki.gateway.oauth2;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsLimitEntity;
import com.quancheng.saluki.gateway.oauth2.entity.GrantTypeEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ScopeEntity;
import com.quancheng.saluki.gateway.oauth2.repository.ClientDetailsRepository;
import com.quancheng.saluki.gateway.oauth2.repository.GrantTypeRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ScopeRepository;
import com.quancheng.saluki.gateway.oauth2.service.OAuth2DatabaseClientDetailsService;

@Configuration
@Profile("default-user-and-roles_route")
public class DefaultClientDetailsConfiguration implements InitializingBean {

    private static final Logger                logger              = LoggerFactory.getLogger(DefaultClientDetailsConfiguration.class);

    private static final String[]              DEFAULT_GRANT_TYPES = { "authorization_code", "refresh_token",
                                                                       "password", "client_credentials" };

    private static final String                API_GRANT_TYPES     = StringUtils.join(DEFAULT_GRANT_TYPES, ",");

    private static final String[]              DEFAULT_SCOPES      = { "read", "write", "trust" };

    private static final String                API_SCOPES          = StringUtils.join(DEFAULT_SCOPES, ",");

    @Autowired
    private GrantTypeRepository                grantTypeRepository;

    @Autowired
    private ScopeRepository                    scopeRepository;

    @Autowired
    private OAuth2DatabaseClientDetailsService oAuth2DatabaseClientDetailsService;

    @Autowired
    private ClientDetailsRepository            clientDetailsRepository;

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
        BaseClientDetails clientDetails = new BaseClientDetails("api", null, API_SCOPES, API_GRANT_TYPES, null);
        clientDetails.setClientSecret("api");
        clientDetails.setRegisteredRedirectUri(Collections.emptySet());
        try {
            oAuth2DatabaseClientDetailsService.addClientDetails(clientDetails);
        } catch (ClientAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }

        clientDetails = new BaseClientDetails("open_api", null, API_SCOPES, API_GRANT_TYPES, null);
        clientDetails.setClientSecret("open_api");
        clientDetails.setRegisteredRedirectUri(Collections.emptySet());
        try {
            oAuth2DatabaseClientDetailsService.addClientDetails(clientDetails);
            // 每隔open_api 10秒内最多只能调用3次api
            ClientDetailsEntity detailEntity = clientDetailsRepository.findOneByClientId(clientDetails.getClientId()).get();
            ClientDetailsLimitEntity limitEntity = ClientDetailsLimitEntity.builder().intervalInMills(10000L).limits(3L).build();
            detailEntity.setClientLimit(limitEntity);
            limitEntity.setClientDetail(detailEntity);
            clientDetailsRepository.save(detailEntity);
        } catch (ClientAlreadyExistsException e) {
            logger.warn(e.getMessage());
        }

    }
}
