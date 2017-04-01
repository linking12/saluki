package com.quancheng.saluki.gateway.oauth2.entity;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "username", callSuper = false)
@ToString(of = "username", callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class UserEntity extends AbstractAuditable<Long> {

    public static final String           NAME_REGEX = "^[A-Za-z0-9_]*$";

    @NotNull
    @Pattern(regexp = NAME_REGEX)
    @Size(max = 50)
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String                       username;

    @NotNull
    @Size(min = 60, max = 60)
    @Column(name = "password_hash", length = 60, nullable = false)
    private String                       password;

    @NotNull
    @Column(nullable = false)
    @ColumnDefault("False")
    private boolean                      disabled;

    @Singular
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserRoleXrefEntity>      roles;

    @Singular
    @OneToMany(mappedBy = "authority", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserAuthorityXrefEntity> authorities;

}
