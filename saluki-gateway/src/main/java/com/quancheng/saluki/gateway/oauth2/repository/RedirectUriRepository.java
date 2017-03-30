package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.RedirectUriEntity;

public interface RedirectUriRepository extends JpaRepository<RedirectUriEntity, Long> {

    Optional<RedirectUriEntity> findOneByValue(String value);
}
