package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findOneByTokenId(String tokenId);

    void deleteByTokenId(String tokenId);
}
