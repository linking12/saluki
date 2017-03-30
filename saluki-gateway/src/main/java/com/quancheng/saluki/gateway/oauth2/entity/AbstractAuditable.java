package com.quancheng.saluki.gateway.oauth2.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.AbstractPersistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.ZonedDateTime;

@Data
@EqualsAndHashCode(of = {}, callSuper = false)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public abstract class AbstractAuditable<PK extends Serializable> extends AbstractPersistable<PK> {

    private static final long serialVersionUID = 1L;

    @NotNull
    @CreatedBy
    @Size(max = 50)
    @Column(name = "created_by", length = 50, updatable = false, nullable = false)
    private String createdBy;

    @NotNull
    @CreatedDate
    @Column(name = "created_date", updatable = false, nullable = false)
    private ZonedDateTime createdDate = ZonedDateTime.now();

    @Size(max = 50)
    @LastModifiedBy
    @Column(name = "last_modified_by", length = 50)
    private String lastModifiedBy;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private ZonedDateTime lastModifiedDate = ZonedDateTime.now();
}
