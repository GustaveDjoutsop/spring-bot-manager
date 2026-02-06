package com.botmanager.core.payment;

import com.botmanager.config.CamPayProperties;
import com.botmanager.core.payment.provider.CamPayProvider;
import com.botmanager.core.payment.provider.MtnMomoProvider;
import com.botmanager.core.payment.provider.PaymentProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentGateway {

    private final CamPayProvider camPayProvider;

    private final MtnMomoProvider mtnMomoProvider;

    private final CamPayProperties camPayProperties;

    private final PaymentStore paymentStore;

    private final PaymentEventPublisher paymentEventPublisher;

    private final Map<String, PaymentProvider> providers = new HashMap<>();

    private String defaultProvider;

    @PostConstruct
    void init() {
        if (camPayProperties.isConfigured()) {
            providers.put(camPayProvider.getName(), camPayProvider);
            defaultProvider = camPayProvider.getName();
            log.info("CamPay provider registered");
        }

        providers.put(mtnMomoProvider.getName(), mtnMomoProvider);

        if (defaultProvider == null) {
            defaultProvider = mtnMomoProvider.getName();
        }

        log.info("Payment gateway initialized with {} providers", providers.size());
    }

    public PaymentResult initiatePayment(PaymentRequest request) {
        PaymentProvider provider = providers.get(defaultProvider);
        if (provider == null) {
            return PaymentResult.builder()
                    .success(false)
                    .errorMessage("No payment provider available")
                    .build();
        }

        PaymentResult result = provider.initiatePayment(request);

        if (result.isSuccess()) {
            PaymentRecord record = PaymentRecord.builder()
                    .botId(request.getBotId())
                    .provider(provider.getName())
                    .transactionId(result.getTransactionId())
                    .externalRef(result.getExternalRef())
                    .customerPhone(request.getPhoneNumber())
                    .amount(request.getAmount())
                    .currency(request.getCurrency())
                    .status(result.getStatus())
                    .metadata(request.getMetadata())
                    .createdAt(Instant.now())
                    .raw(result.getRaw())
                    .build();

            paymentStore.upsertPayment(record);
            paymentEventPublisher.publishInitiated(record);
        }

        return result;
    }

    public PaymentStatus checkStatus(String botId, String provider, String transactionId) {
        PaymentProvider paymentProvider = providers.get(provider);
        if (paymentProvider == null) {
            return PaymentStatus.PENDING;
        }

        return paymentProvider.checkStatus(transactionId);
    }

    public PaymentResult handleWebhook(String botId, String providerName, Map<String, Object> payload) {
        PaymentProvider provider = providers.get(providerName);
        if (provider == null) {
            return PaymentResult.builder()
                    .success(false)
                    .errorMessage("Unknown provider: " + providerName)
                    .build();
        }

        PaymentResult result = provider.handleWebhook(payload);

        if (result.isSuccess() && result.getTransactionId() != null) {
            paymentStore.getPayment(botId, result.getTransactionId())
                    .ifPresent(record -> {
                        record.setStatus(result.getStatus());
                        record.setRaw(result.getRaw());
                        paymentStore.upsertPayment(record);
                        paymentEventPublisher.publishStatusUpdate(record);
                    });
        }

        return result;
    }

}
