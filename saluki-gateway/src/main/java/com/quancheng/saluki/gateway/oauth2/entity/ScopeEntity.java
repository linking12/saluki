package com.quancheng.saluki.gateway.oauth2.entity;

import lombok.*;
import org.springframework.data.jpa.domain.AbstractPersistable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "value", callSuper = false)
@ToString(of = "value", callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "scope")
public class ScopeEntity extends AbstractPersistable<Long> {

    @NotNull
    @Column(name = "value", nullable = false)
    private String value;

    @OneToMany(mappedBy = "scope", fetch = FetchType.LAZY)
    @Singular
    private Set<ClientDetailsToScopesXrefEntity> clientDetailsToScopesXrefs;

}
