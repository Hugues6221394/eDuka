package com.inzozi.chat.security;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class JsonUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {}

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parse(String json) throws Exception {
        return MAPPER.readValue(json, Map.class);
    }
}
