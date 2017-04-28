package com.quancheng.saluki.gateway.oauth2.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;

import javax.persistence.AttributeConverter;

public class OAuth2RefreshTokenPersistenceConverters implements AttributeConverter<OAuth2RefreshToken, String> {

    private JsonPersistenceConverters<OAuth2RefreshToken> jsonPersistenceConverters = new JsonPersistenceConverters<OAuth2RefreshToken>() {
        @Override
        protected void configureObjectMapper(ObjectMapper objectMapper) {
            SimpleModule module = new SimpleModule();
            module.addSerializer(OAuth2RefreshToken.class, new OAuth2RefreshTokenJackson2SerializerDeserializer.OAuth2RefreshTokenJackson2Serializer());
            module.addDeserializer(OAuth2RefreshToken.class, new OAuth2RefreshTokenJackson2SerializerDeserializer.OAuth2RefreshTokenJackson2Deserializer());

            objectMapper.registerModule(module);
        }
    };

    @Override
    public String convertToDatabaseColumn(OAuth2RefreshToken attribute) {
        return jsonPersistenceConverters.convertToJson(attribute);
    }

    @Override
    public OAuth2RefreshToken convertToEntityAttribute(String dbData) {
        return jsonPersistenceConverters.convertFromJson(dbData, OAuth2RefreshToken.class);
    }
}
