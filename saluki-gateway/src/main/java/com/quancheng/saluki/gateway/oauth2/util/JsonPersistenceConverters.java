package com.quancheng.saluki.gateway.oauth2.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
public class JsonPersistenceConverters<IN> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonPersistenceConverters() {
        configureObjectMapper(objectMapper);
    }

    protected void configureObjectMapper(ObjectMapper objectMapper) {

    }

    protected final String convertToJson(IN input) {
        if (input == null) return null;
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException e) {
            log.error("Serialize " + input + " error.", e);
            throw new RuntimeException(e);
        }
    }

    protected final IN convertFromJson(String json, Class<? extends IN> typeOfInput) {
        if (StringUtils.isEmpty(json)) return null;

        try {
            return objectMapper.readValue(json, typeOfInput);
        } catch (IOException e) {
            log.error("Deserialize OAuth2AccessToken error.", e);
            throw new RuntimeException(e);
        }
    }

}
