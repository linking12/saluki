package com.quancheng.saluki.gateway.oauth2.util;

import javax.persistence.AttributeConverter;

import org.springframework.security.oauth2.common.OAuth2AccessToken;

public class OAuth2AccessTokenPersistenceConverters
    implements AttributeConverter<OAuth2AccessToken, String> {

  private JsonPersistenceConverters<OAuth2AccessToken> jsonPersistenceConverters =
      new JsonPersistenceConverters<>();

  @Override
  public String convertToDatabaseColumn(OAuth2AccessToken attribute) {
    return jsonPersistenceConverters.convertToJson(attribute);
  }

  @Override
  public OAuth2AccessToken convertToEntityAttribute(String dbData) {
    return jsonPersistenceConverters.convertFromJson(dbData, OAuth2AccessToken.class);
  }
}
