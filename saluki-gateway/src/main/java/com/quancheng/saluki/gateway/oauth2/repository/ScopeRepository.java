package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.ScopeEntity;

public interface ScopeRepository extends JpaRepository<ScopeEntity, Long> {

    Optional<ScopeEntity> findOneByValue(String value);
}
