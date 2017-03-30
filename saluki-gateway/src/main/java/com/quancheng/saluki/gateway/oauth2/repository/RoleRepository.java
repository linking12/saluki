package com.quancheng.saluki.gateway.oauth2.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    Optional<RoleEntity> findOneByName(String roleName);

}
