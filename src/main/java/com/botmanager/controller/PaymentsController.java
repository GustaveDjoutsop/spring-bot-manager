package com.botmanager.controller;

import com.botmanager.config.CamPayProperties;
import com.botmanager.core.payment.PaymentGateway;
import com.botmanager.core.payment.PaymentRecord;
import com.botmanager.core.payment.PaymentResult;
import com.botmanager.core.payment.PaymentStore;
import com.botmanager.core.payment.WebhookSignatureVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentsController {

    private final PaymentGateway paymentGateway;

    private final PaymentStore paymentStore;

    private final WebhookSignatureVerifier signatureVerifier;

    private final CamPayProperties camPayProperties;

    private final ObjectMapper objectMapper;

    @PostMapping("/webhooks/campay/{botId}")
    public ResponseEntity<Map<String, Object>> handleCamPayWebhook(
            @PathVariable String botId,
            @RequestBody String rawBody,
            @RequestHeader(value = "x-campay-signature", required = false) String signature) {

        log.debug("Received CamPay webhook for bot {}", botId);

        if (StringUtils.hasText(camPayProperties.getWebhookSecret())) {
            if (!signatureVerifier.verifyHmacSha256(camPayProperties.getWebhookSecret(), rawBody, signature)) {
                log.warn("Invalid CamPay webhook signature for bot {}", botId);

                return ResponseEntity.status(401).body(Map.of("error", "Invalid signature"));
            }
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(rawBody, Map.class);
            PaymentResult result = paymentGateway.handleWebhook(botId, "campay", payload);

            if (result.isSuccess()) {
                return ResponseEntity.ok(Map.of("status", "received"));
            }

            return ResponseEntity.status(400).body(Map.of("error", result.getErrorMessage()));
        } catch (Exception exception) {
            log.error("Failed to process CamPay webhook: {}", exception.getMessage());

            return ResponseEntity.status(500).body(Map.of("error", "Processing error"));
        }
    }

    @GetMapping("/{botId}/transactions/{transactionId}")
    public ResponseEntity<Object> getTransaction(@PathVariable String botId,
                                                 @PathVariable String transactionId) {

        return paymentStore.getPayment(botId, transactionId)
                .map(record -> ResponseEntity.ok((Object) record))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{botId}/external/{externalRef}")
    public ResponseEntity<Object> getTransactionByExternalRef(@PathVariable String botId,
                                                              @PathVariable String externalRef) {

        return paymentStore.getPaymentByExternalRef(botId, externalRef)
                .map(record -> ResponseEntity.ok((Object) record))
                .orElse(ResponseEntity.notFound().build());
    }

}
