package com.botmanager.core.payment.provider;

import com.botmanager.config.CamPayProperties;
import com.botmanager.core.payment.PaymentRequest;
import com.botmanager.core.payment.PaymentResult;
import com.botmanager.core.payment.PaymentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CamPayProvider extends PaymentProvider {

    private final CamPayProperties camPayProperties;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    @Override
    public String getName() {
        return "campay";
    }

    @Override
    public PaymentResult initiatePayment(PaymentRequest request) {
        if (!camPayProperties.isConfigured()) {
            return PaymentResult.builder()
                    .success(false)
                    .errorMessage("CamPay not configured")
                    .build();
        }

        try {
            String url = camPayProperties.getBaseUrl() + camPayProperties.getCollectPath();

            Map<String, Object> payload = new HashMap<>();
            payload.put("amount", String.valueOf(request.getAmount()));
            payload.put("currency", request.getCurrency());
            payload.put("from", request.getPhoneNumber());
            payload.put("description", request.getDescription());
            payload.put("external_reference", request.getReference());

            HttpHeaders headers = createHeaders();
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String transactionId = (String) body.get("reference");
                String status = (String) body.get("status");

                return PaymentResult.builder()
                        .success(true)
                        .transactionId(transactionId)
                        .externalRef(request.getReference())
                        .status(PaymentStatus.fromValue(status))
                        .raw(body)
                        .build();
            }

            return PaymentResult.builder()
                    .success(false)
                    .errorMessage("CamPay request failed")
                    .build();
        } catch (Exception exception) {
            log.error("CamPay initiate payment failed: {}", exception.getMessage());

            return PaymentResult.builder()
                    .success(false)
                    .errorMessage(exception.getMessage())
                    .build();
        }
    }

    @Override
    public PaymentStatus checkStatus(String transactionId) {
        if (!camPayProperties.isConfigured()) {
            return PaymentStatus.PENDING;
        }

        try {
            String url = camPayProperties.getBaseUrl() + camPayProperties.getStatusPath() + transactionId + "/";

            HttpHeaders headers = createHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> body = response.getBody();
                String status = (String) body.get("status");

                if (status == null) {
                    status = (String) body.get("state");
                }

                return PaymentStatus.fromValue(status);
            }

            return PaymentStatus.PENDING;
        } catch (Exception exception) {
            log.error("CamPay check status failed for {}: {}", transactionId, exception.getMessage());

            return PaymentStatus.PENDING;
        }
    }

    @Override
    public PaymentResult handleWebhook(Map<String, Object> payload) {
        String transactionId = (String) payload.get("reference");
        String status = (String) payload.get("status");

        if (status == null) {
            status = (String) payload.get("state");
        }

        String externalRef = (String) payload.get("external_reference");

        return PaymentResult.builder()
                .success(true)
                .transactionId(transactionId)
                .externalRef(externalRef)
                .status(PaymentStatus.fromValue(status))
                .raw(payload)
                .build();
    }

    private HttpHeaders createHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", camPayProperties.getAuthScheme() + " " + camPayProperties.getToken());
        headers.set("Content-Type", "application/json");

        return headers;
    }

}
