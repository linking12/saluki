package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.ResourceIdEntity;

public interface ResourceIdRepository extends JpaRepository<ResourceIdEntity, Long> {

    Optional<ResourceIdEntity> findOneByValue(String value);
}
