package com.quancheng.saluki.gateway.oauth2.entity;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "clientId", callSuper = false)
@ToString(exclude = "clientSecret", callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "client_details")
public class ClientDetailsEntity extends AbstractAuditable<Long> {

    @NonNull
    @NotNull
    @Column(name = "client_id", unique = true, nullable = false, length = 200)
    private String                                            clientId;

    @NonNull
    @NotNull
    @Column(name = "client_secret", nullable = false)
    private String                                            clientSecret;

    @Column(name = "access_token_validity_seconds")
    private Integer                                           accessTokenValiditySeconds;

    @Column(name = "refresh_token_validity_seconds")
    private Integer                                           refreshTokenValiditySeconds;

    @Singular
    @OneToMany(mappedBy = "clientDetails", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ClientDetailsToAuthorizedGrantTypeXrefEntity> authorizedGrantTypeXrefs;

    @Singular
    @OneToMany(mappedBy = "clientDetails", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ClientDetailsToScopesXrefEntity>              scopeXrefs;

    @Singular
    @OneToMany(mappedBy = "clientDetails", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<ClientDetailsToResourceIdXrefEntity>          resourceIdXrefs;

    @Singular("redirectUri")
    @OneToMany(mappedBy = "clientDetails", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private Set<RedirectUriEntity>                            redirectUris;

    @OneToOne(mappedBy = "clientDetail", fetch = FetchType.EAGER, orphanRemoval = true, cascade = CascadeType.ALL)
    private ClientDetailsLimitEntity                                 clientLimit;

}
