package com.quancheng.saluki.gateway.oauth2.entity;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(of = {"clientDetails", "grantType"}, callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "client_details_grant_type_xref")
public class ClientDetailsToAuthorizedGrantTypeXrefEntity extends AbstractAuditable<Long> {

    @NonNull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "client_details_id")
    private ClientDetailsEntity clientDetails;

    @NonNull
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "grant_type_id")
    private GrantTypeEntity grantType;

}
