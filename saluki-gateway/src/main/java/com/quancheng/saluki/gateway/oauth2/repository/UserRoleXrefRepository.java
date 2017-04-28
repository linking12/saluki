package com.quancheng.saluki.gateway.oauth2.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.quancheng.saluki.gateway.oauth2.entity.UserRoleXrefEntity;

public interface UserRoleXrefRepository extends JpaRepository<UserRoleXrefEntity, Long> {
}
