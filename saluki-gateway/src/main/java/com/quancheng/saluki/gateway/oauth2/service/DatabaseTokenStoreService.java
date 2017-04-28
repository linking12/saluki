package com.quancheng.saluki.gateway.oauth2.service;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.quancheng.saluki.gateway.oauth2.entity.AccessTokenEntity;
import com.quancheng.saluki.gateway.oauth2.entity.RefreshTokenEntity;
import com.quancheng.saluki.gateway.oauth2.repository.AccessTokenRepository;
import com.quancheng.saluki.gateway.oauth2.repository.RefreshTokenRepository;

@Service
@Transactional
public class DatabaseTokenStoreService implements TokenStore {

    @Autowired
    private AccessTokenRepository      accessTokenRepository;

    @Autowired
    private RefreshTokenRepository     refreshTokenRepository;

    private AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken token) {
        return readAuthentication(token.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token) {
        return accessTokenRepository.findOneByTokenId(token).map(AccessTokenEntity::getAuthentication).orElse(null);
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {

        String tokenId = token.getValue();

        final RefreshTokenEntity refreshToken;
        String authenticationKey = authenticationKeyGenerator.extractKey(authentication);

        if (token.getRefreshToken() != null) {
            refreshToken = refreshTokenRepository.findOneByTokenId(token.getRefreshToken().getValue()).orElseGet(() -> refreshTokenRepository.save(RefreshTokenEntity.builder().tokenId(token.getRefreshToken().getValue()).token(token.getRefreshToken()).authentication(authentication).build()));
        } else {
            refreshToken = null;
        }

        accessTokenRepository.findOneByAuthenticationId(authenticationKey).ifPresent(accessTokenEntity -> {
            if (!tokenId.equals(accessTokenEntity.getTokenId())) {
                accessTokenRepository.delete(accessTokenEntity);
            }
        });

        AccessTokenEntity entityToSave = accessTokenRepository.findOneByTokenId(tokenId).map(accessTokenEntity -> {
            accessTokenEntity.setToken(token);
            accessTokenEntity.setAuthenticationId(authenticationKey);
            accessTokenEntity.setAuthentication(authentication);
            accessTokenEntity.setUserName(authentication.isClientOnly() ? null : authentication.getName());
            accessTokenEntity.setClientId(authentication.getOAuth2Request().getClientId());
            accessTokenEntity.setRefreshToken(refreshToken);
            return accessTokenEntity;
        }).orElseGet(() -> AccessTokenEntity.builder().tokenId(tokenId).token(token).authenticationId(authenticationKey).authentication(authentication).userName(authentication.isClientOnly() ? null : authentication.getName()).clientId(authentication.getOAuth2Request().getClientId()).refreshToken(refreshToken).build());

        accessTokenRepository.save(entityToSave);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue) {
        return accessTokenRepository.findOneByTokenId(tokenValue).map(AccessTokenEntity::getToken).orElse(null);
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken token) {
        accessTokenRepository.deleteByTokenId(token.getValue());
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication) {
        RefreshTokenEntity entityToSave = refreshTokenRepository.findOneByTokenId(refreshToken.getValue()).map(refreshTokenEntity -> {
            refreshTokenEntity.setToken(refreshToken);
            refreshTokenEntity.setAuthentication(authentication);
            return refreshTokenEntity;
        }).orElseGet(() -> RefreshTokenEntity.builder().tokenId(refreshToken.getValue()).token(refreshToken).authentication(authentication).build());

        refreshTokenRepository.save(entityToSave);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue) {
        return refreshTokenRepository.findOneByTokenId(tokenValue).map(RefreshTokenEntity::getToken).orElse(null);
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken token) {
        return refreshTokenRepository.findOneByTokenId(token.getValue()).map(RefreshTokenEntity::getAuthentication).orElse(null);
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken token) {
        refreshTokenRepository.deleteByTokenId(token.getValue());
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken) {
        accessTokenRepository.deleteByRefreshTokenTokenId(refreshToken.getValue());
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication) {
        String authenticationKey = authenticationKeyGenerator.extractKey(authentication);
        return accessTokenRepository.findOneByAuthenticationId(authenticationKey).map(AccessTokenEntity::getToken).orElse(null);
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String userName) {
        return accessTokenRepository.findAllByClientIdAndUserName(clientId,
                                                                  userName).stream().map(AccessTokenEntity::getToken).collect(Collectors.toList());
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId) {
        return accessTokenRepository.findAllByClientId(clientId).stream().map(AccessTokenEntity::getToken).collect(Collectors.toList());
    }
}
