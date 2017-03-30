package com.quancheng.saluki.gateway.oauth2.entity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.quancheng.saluki.gateway.oauth2.util.OAuth2AccessTokenPersistenceConverters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "tokenId", callSuper = false)
@ToString(exclude = { "token", "authentication", "refreshToken" }, callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "access_token")
public class AccessTokenEntity extends AbstractAuditable<Long> {

    @NonNull
    @NotNull
    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String               tokenId;

    @NonNull
    @NotNull
    @Convert(converter = OAuth2AccessTokenPersistenceConverters.class)
    @Column(name = "serialized_token", nullable = false)
    private OAuth2AccessToken    token;

    @NonNull
    @NotNull
    @Column(name = "authentication_id", nullable = false, length = 32)
    private String               authenticationId;

    @Size(max = 50)
    @Column(name = "user_name", length = 50)
    private String               userName;

    @Size(max = 200)
    @Column(name = "client_id", length = 200)
    private String               clientId;

    @NonNull
    @NotNull
    @Lob
    @Column(name = "serialized_authentication", nullable = false)
    private OAuth2Authentication authentication;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "refresh_token_id", nullable = true)
    private RefreshTokenEntity   refreshToken;

}
