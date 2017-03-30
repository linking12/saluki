package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.GrantTypeEntity;

public interface GrantTypeRepository extends JpaRepository<GrantTypeEntity, Long> {

    Optional<GrantTypeEntity> findOneByValue(String value);
}
