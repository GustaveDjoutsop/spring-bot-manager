package com.botmanager.core.whatsapp;

import com.botmanager.config.WhatsAppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppClientFactory {

    private final WhatsAppProperties whatsAppProperties;

    private final RestTemplate restTemplate;

    private final Environment environment;

    private final Map<String, WhatsAppClient> clientCache = new ConcurrentHashMap<>();

    public WhatsAppClient getClient(String botId, String phoneNumberId) {
        String cacheKey = botId + ":" + phoneNumberId;

        return clientCache.computeIfAbsent(cacheKey, key -> {
            String accessToken = getAccessToken(botId);

            if (accessToken == null) {
                log.error("No access token found for bot {}", botId);

                return null;
            }

            return new WhatsAppClient(phoneNumberId, accessToken, whatsAppProperties, restTemplate);
        });
    }

    private String getAccessToken(String botId) {
        String envKey = "WHATSAPP_ACCESS_TOKEN_" + botId.toUpperCase().replace("-", "_");
        String token = environment.getProperty(envKey);

        if (token == null) {
            token = environment.getProperty("whatsapp.access-token." + botId);
        }

        return token;
    }

}
