package com.quancheng.saluki.gateway.oauth2.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import com.quancheng.saluki.gateway.oauth2.util.OAuth2RefreshTokenPersistenceConverters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "tokenId", callSuper = false)
@ToString(of = "tokenId", callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity extends AbstractAuditable<Long> {

    @NonNull
    @NotNull
    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String tokenId;

    @NonNull
    @NotNull
    @Convert(converter = OAuth2RefreshTokenPersistenceConverters.class)
    @Column(name = "serialized_token", nullable = false)
    private OAuth2RefreshToken token;

    @NonNull
    @NotNull
    @Lob
    @Column(name = "serialized_authentication", nullable = false)
    private OAuth2Authentication authentication;

    @OneToMany(mappedBy = "refreshToken", fetch = FetchType.LAZY)
    private Set<AccessTokenEntity> accessTokens;

}
