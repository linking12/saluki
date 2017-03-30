package com.quancheng.saluki.gateway.oauth2.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Where;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Data
@EqualsAndHashCode(of = "authority", callSuper = true)
@ToString(of = "authority", callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user_authority_xref")
public class UserAuthorityXrefEntity extends AbstractAuditable<Long> {

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER, optional = false, cascade = {CascadeType.DETACH, CascadeType.REFRESH})
    @JoinColumn(name = "authority_id", nullable = false)
    @Where(clause = "disabled = False")
    private AuthorityEntity authority;

}
