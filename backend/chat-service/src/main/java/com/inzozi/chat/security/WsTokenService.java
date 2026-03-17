package com.inzozi.chat.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class WsTokenService {

    @Value("${chat.ws.secret}")
    private String secret;

    public String validateAndGetUserId(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            String[] parts = token.split("\.");
            if (parts.length != 3) return null;
            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String expectedSig = hmacSha256(header + "." + payload, secret);
            if (!constantTimeEquals(signature, expectedSig)) return null;

            String json = new String(Base64.getUrlDecoder().decode(payload), StandardCharsets.UTF_8);
            Map<String, Object> map = JsonUtils.parse(json);
            Object exp = map.get("exp");
            Object sub = map.get("sub");
            if (sub == null || exp == null) return null;
            long expSeconds = Long.parseLong(exp.toString());
            if (Instant.now().getEpochSecond() > expSeconds) return null;
            return sub.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    private boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null || a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }
}
