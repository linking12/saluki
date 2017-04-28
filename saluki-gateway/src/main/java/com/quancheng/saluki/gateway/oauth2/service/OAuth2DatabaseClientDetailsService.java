package com.quancheng.saluki.gateway.oauth2.service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.ClientAlreadyExistsException;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.ClientRegistrationService;
import org.springframework.security.oauth2.provider.NoSuchClientException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToAuthorizedGrantTypeXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToResourceIdXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.ClientDetailsToScopesXrefEntity;
import com.quancheng.saluki.gateway.oauth2.entity.RedirectUriEntity;
import com.quancheng.saluki.gateway.oauth2.repository.ClientDetailsRepository;
import com.quancheng.saluki.gateway.oauth2.repository.GrantTypeRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ResourceIdRepository;
import com.quancheng.saluki.gateway.oauth2.repository.ScopeRepository;

@Service
public class OAuth2DatabaseClientDetailsService implements ClientDetailsService, ClientRegistrationService {

    @Autowired
    private ClientDetailsRepository clientDetailsRepository;

    @Autowired
    private GrantTypeRepository     grantTypeRepository;

    @Autowired
    private ScopeRepository         scopeRepository;

    @Autowired
    private ResourceIdRepository    resourceIdRepository;

    @Autowired
    private PasswordEncoder         passwordEncoder;

    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return clientDetailsRepository.findOneByClientId(clientId).map(entityToDomain).<ClientRegistrationException> orElseThrow(() -> new NoSuchClientException("Client ID not found"));
    }

    @Transactional
    @Override
    public void addClientDetails(ClientDetails clientDetails) throws ClientAlreadyExistsException {
        if (clientDetailsRepository.findOneByClientId(clientDetails.getClientId()).isPresent()) {
            throw new ClientAlreadyExistsException("Client ID already exists");
        }

        ClientDetailsEntity clientDetailsEntity = ClientDetailsEntity.builder()//
                                                                     .clientId(clientDetails.getClientId())//
                                                                     .clientSecret(clientDetails.getClientSecret())//
                                                                     .accessTokenValiditySeconds(clientDetails.getAccessTokenValiditySeconds())//
                                                                     .refreshTokenValiditySeconds(clientDetails.getRefreshTokenValiditySeconds()).build();

        clientDetailsEntity.setAuthorizedGrantTypeXrefs(clientDetails.getAuthorizedGrantTypes().stream().map(grantType -> grantTypeRepository.findOneByValue(grantType).map(grantTypeEntity -> ClientDetailsToAuthorizedGrantTypeXrefEntity.builder().clientDetails(clientDetailsEntity).grantType(grantTypeEntity).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unsupported grant type: "
                                                                                                                                                                                                                                                                                                                                                                                                             + grantType))).collect(Collectors.toSet()));

        clientDetailsEntity.setScopeXrefs(clientDetails.getScope().stream().map(scope -> scopeRepository.findOneByValue(scope).map(scopeEntity -> ClientDetailsToScopesXrefEntity.builder().clientDetails(clientDetailsEntity).scope(scopeEntity).autoApprove(clientDetails.isAutoApprove(scope)).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unknown scope: "
                                                                                                                                                                                                                                                                                                                                                                                           + scope))).collect(Collectors.toSet()));

        clientDetailsEntity.setResourceIdXrefs(clientDetails.getResourceIds().stream().map(resourceId -> resourceIdRepository.findOneByValue(resourceId).map(resourceIdEntity -> ClientDetailsToResourceIdXrefEntity.builder().clientDetails(clientDetailsEntity).resourceId(resourceIdEntity).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unknown resource id: "
                                                                                                                                                                                                                                                                                                                                                                                        + resourceId))).collect(Collectors.toSet()));

        clientDetailsEntity.setRedirectUris(clientDetails.getRegisteredRedirectUri().stream().map(redirectUri -> RedirectUriEntity.builder().clientDetails(clientDetailsEntity).value(redirectUri).build()).collect(Collectors.toSet()));

        clientDetailsRepository.save(clientDetailsEntity);

    }

    @Transactional
    @Override
    public void updateClientDetails(ClientDetails clientDetails) throws NoSuchClientException {

        ClientDetailsEntity entity = clientDetailsRepository.findOneByClientId(clientDetails.getClientId()).orElseThrow(() -> new NoSuchClientException("Client details not found."));

        entity.setAccessTokenValiditySeconds(clientDetails.getAccessTokenValiditySeconds());
        entity.setRefreshTokenValiditySeconds(clientDetails.getRefreshTokenValiditySeconds());

        // merge grant type
        Set<ClientDetailsToAuthorizedGrantTypeXrefEntity> grantTypeXrefEntityRemoves = entity.getAuthorizedGrantTypeXrefs().stream().filter(grantTypeXrefEntity -> !clientDetails.getAuthorizedGrantTypes().contains(grantTypeXrefEntity.getGrantType().getValue())).collect(Collectors.toSet());

        Set<String> grantTypeOriginValueSet = entity.getAuthorizedGrantTypeXrefs().stream().map(xref -> xref.getGrantType().getValue()).collect(Collectors.toSet());
        Set<ClientDetailsToAuthorizedGrantTypeXrefEntity> grantTypeXrefEntityNewOnes = clientDetails.getAuthorizedGrantTypes().stream().filter(grantType -> !grantTypeOriginValueSet.contains(grantType)).map(grantType -> grantTypeRepository.findOneByValue(grantType).map(grantTypeEntity -> ClientDetailsToAuthorizedGrantTypeXrefEntity.builder().clientDetails(entity).grantType(grantTypeEntity).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unsupported grant type: "
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 + grantType))).collect(Collectors.toSet());

        entity.getAuthorizedGrantTypeXrefs().removeAll(grantTypeXrefEntityRemoves);
        entity.getAuthorizedGrantTypeXrefs().addAll(grantTypeXrefEntityNewOnes);

        // merge scopes
        Set<ClientDetailsToScopesXrefEntity> scopeXrefEntityRemoves = entity.getScopeXrefs().stream().filter(clientDetailsToScopesXrefEntity -> !clientDetails.getScope().contains(clientDetailsToScopesXrefEntity.getScope().getValue())).collect(Collectors.toSet());

        Set<String> scopeOriginValueSet = entity.getScopeXrefs().stream().map(xref -> xref.getScope().getValue()).collect(Collectors.toSet());
        Set<ClientDetailsToScopesXrefEntity> scopeXrefEntityNewOnes = clientDetails.getScope().stream().filter(scope -> !scopeOriginValueSet.contains(scope)).map(scope -> scopeRepository.findOneByValue(scope).map(scopeEntity -> ClientDetailsToScopesXrefEntity.builder().clientDetails(entity).scope(scopeEntity).autoApprove(clientDetails.isAutoApprove(scope)).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unknown scope: "
                                                                                                                                                                                                                                                                                                                                                                                                                                                                + scope))).collect(Collectors.toSet());

        entity.getScopeXrefs().removeAll(scopeXrefEntityRemoves);
        entity.getScopeXrefs().forEach(xref -> xref.setAutoApprove(clientDetails.isAutoApprove(xref.getScope().getValue())));
        entity.getScopeXrefs().addAll(scopeXrefEntityNewOnes);

        // merge resource id
        Set<ClientDetailsToResourceIdXrefEntity> resourceIdXrefEntityRemoves = entity.getResourceIdXrefs().stream().filter(xref -> !clientDetails.getResourceIds().contains(xref.getResourceId().getValue())).collect(Collectors.toSet());

        Set<String> resIdOriginValueSet = entity.getResourceIdXrefs().stream().map(xref -> xref.getResourceId().getValue()).collect(Collectors.toSet());
        Set<ClientDetailsToResourceIdXrefEntity> resIdXrefEntityNewOnes = clientDetails.getResourceIds().stream().filter(resId -> !resIdOriginValueSet.contains(resId)).map(resId -> resourceIdRepository.findOneByValue(resId).map(resourceIdEntity -> ClientDetailsToResourceIdXrefEntity.builder().clientDetails(entity).resourceId(resourceIdEntity).build()).<ClientRegistrationException> orElseThrow(() -> new ClientRegistrationException("Unknown resource id: "
                                                                                                                                                                                                                                                                                                                                                                                                                                                  + resId))).collect(Collectors.toSet());

        entity.getResourceIdXrefs().removeAll(resourceIdXrefEntityRemoves);
        entity.getResourceIdXrefs().addAll(resIdXrefEntityNewOnes);

        // merge redirect uri
        Set<RedirectUriEntity> redirectUriEntityRemoves = entity.getRedirectUris().stream().filter(redirectUriEntity -> !clientDetails.getRegisteredRedirectUri().contains(redirectUriEntity.getValue())).collect(Collectors.toSet());

        Set<String> originRedirectUrisValue = entity.getRedirectUris().stream().map(RedirectUriEntity::getValue).collect(Collectors.toSet());
        Set<RedirectUriEntity> redirectUriEntityNewOnes = clientDetails.getRegisteredRedirectUri().stream().filter(redirectUri -> !originRedirectUrisValue.contains(redirectUri)).map(redirectUri -> RedirectUriEntity.builder().clientDetails(entity).value(redirectUri).build()).collect(Collectors.toSet());

        entity.getRedirectUris().removeAll(redirectUriEntityRemoves);
        entity.getRedirectUris().addAll(redirectUriEntityNewOnes);

        // save
        clientDetailsRepository.save(entity);
    }

    @Transactional
    @Override
    public void updateClientSecret(String clientId, String secret) throws NoSuchClientException {
        ClientDetailsEntity clientDetailsEntity = clientDetailsRepository.findOneByClientId(clientId).<NoSuchClientException> orElseThrow(() -> new NoSuchClientException("Client id not found."));

        clientDetailsEntity.setClientSecret(passwordEncoder.encode(secret));

        clientDetailsRepository.save(clientDetailsEntity);
    }

    @Transactional
    @Override
    public void removeClientDetails(String clientId) throws NoSuchClientException {

        ClientDetailsEntity entityToRemove = clientDetailsRepository.findOneByClientId(clientId).<NoSuchClientException> orElseThrow(() -> new NoSuchClientException("Client id not found."));

        clientDetailsRepository.delete(entityToRemove);

    }

    @Override
    public List<ClientDetails> listClientDetails() {
        return clientDetailsRepository.findAll().stream().map(entityToDomain).collect(Collectors.toList());
    }

    private final Function<? super ClientDetailsEntity, ? extends BaseClientDetails> entityToDomain = entity -> {
        BaseClientDetails clientDetails = new BaseClientDetails();

        clientDetails.setClientId(entity.getClientId());
        clientDetails.setClientSecret(entity.getClientSecret());

        clientDetails.setAccessTokenValiditySeconds(entity.getAccessTokenValiditySeconds());
        clientDetails.setRefreshTokenValiditySeconds(entity.getRefreshTokenValiditySeconds());

        clientDetails.setAuthorizedGrantTypes(entity.getAuthorizedGrantTypeXrefs().stream().map(grantTypeXrefEntity -> grantTypeXrefEntity.getGrantType().getValue()).collect(Collectors.toList()));

        clientDetails.setScope(entity.getScopeXrefs().stream().map(scopeXrefEntity -> scopeXrefEntity.getScope().getValue()).collect(Collectors.toList()));

        clientDetails.setAutoApproveScopes(entity.getScopeXrefs().stream().filter(ClientDetailsToScopesXrefEntity::getAutoApprove).map(scopeXrefEntity -> scopeXrefEntity.getScope().getValue()).collect(Collectors.toList()));

        clientDetails.setResourceIds(entity.getResourceIdXrefs().stream().map(resXref -> resXref.getResourceId().getValue()).collect(Collectors.toList()));

        clientDetails.setRegisteredRedirectUri(entity.getRedirectUris().stream().map(RedirectUriEntity::getValue).collect(Collectors.toSet()));

        clientDetails.setAdditionalInformation(Collections.<String, Object> emptyMap());

        return clientDetails;
    };

}
