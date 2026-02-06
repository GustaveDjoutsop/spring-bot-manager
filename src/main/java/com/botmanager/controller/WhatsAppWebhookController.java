package com.botmanager.controller;

import com.botmanager.core.bot.BotRegistry;
import com.botmanager.core.whatsapp.WhatsAppSignatureVerifier;
import com.botmanager.handler.WhatsAppWebhookHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/whatsapp/webhook")
@RequiredArgsConstructor
public class WhatsAppWebhookController {

    private final BotRegistry botRegistry;

    private final WhatsAppSignatureVerifier signatureVerifier;

    private final WhatsAppWebhookHandler webhookHandler;

    private final ObjectMapper objectMapper;

    @GetMapping
    public ResponseEntity<String> verify(@RequestParam("hub.mode") String mode,
                                         @RequestParam("hub.verify_token") String verifyToken,
                                         @RequestParam("hub.challenge") String challenge) {

        log.debug("WhatsApp webhook verification: mode={}, token={}", mode, verifyToken);

        if (!"subscribe".equals(mode)) {
            log.warn("Invalid hub.mode: {}", mode);

            return ResponseEntity.badRequest().body("Invalid mode");
        }

        if (botRegistry.getBotNameByVerifyToken(verifyToken).isEmpty()) {
            log.warn("Invalid verify token");

            return ResponseEntity.status(403).body("Invalid token");
        }

        log.info("WhatsApp webhook verified successfully");

        return ResponseEntity.ok(challenge);
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature) {

        if (!signatureVerifier.verify(signature, rawBody)) {
            log.warn("Invalid webhook signature");

            return ResponseEntity.status(401).body("Invalid signature");
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(rawBody, Map.class);
            webhookHandler.handleWebhook(payload);

            return ResponseEntity.ok("EVENT_RECEIVED");
        } catch (Exception exception) {
            log.error("Failed to process webhook: {}", exception.getMessage());

            return ResponseEntity.status(500).body("Processing error");
        }
    }

}
