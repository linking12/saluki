package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.AccessTokenEntity;


public interface AccessTokenRepository extends JpaRepository<AccessTokenEntity, Long> {

    Optional<AccessTokenEntity> findOneByTokenId(String tokenId);

    Optional<AccessTokenEntity> findOneByAuthenticationId(String authenticationId);

    void deleteByTokenId(String tokenId);

    void deleteByRefreshTokenTokenId(String refreshTokenId);

    List<AccessTokenEntity> findAllByClientIdAndUserName(String clientId, String userName);

    List<AccessTokenEntity> findAllByClientId(String clientId);

}
