package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findOneByUsername(String username);
}
