package com.botmanager.core.payment;

import com.botmanager.config.PaymentProperties;
import com.botmanager.core.redis.RedisManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentStore {

    private static final String PAYMENT_KEY_PREFIX = "payment:";

    private static final String PAYMENT_REF_KEY_PREFIX = "paymentRef:";

    private final RedisManager redisManager;

    private final PaymentProperties paymentProperties;

    public void upsertPayment(PaymentRecord record) {
        String key = PAYMENT_KEY_PREFIX + record.getBotId() + ":" + record.getTransactionId();

        if (record.getCreatedAt() == null) {
            record.setCreatedAt(Instant.now());
        }

        record.setUpdatedAt(Instant.now());

        redisManager.setWithExpiry(key, record, paymentProperties.getTtlSeconds());

        if (record.getExternalRef() != null) {
            String refKey = PAYMENT_REF_KEY_PREFIX + record.getBotId() + ":" + record.getExternalRef();
            redisManager.setWithExpiry(refKey, record.getTransactionId(), paymentProperties.getTtlSeconds());
        }

        log.debug("Upserted payment {} for bot {}", record.getTransactionId(), record.getBotId());
    }

    public Optional<PaymentRecord> getPayment(String botId, String transactionId) {
        String key = PAYMENT_KEY_PREFIX + botId + ":" + transactionId;

        return redisManager.get(key, PaymentRecord.class);
    }

    public Optional<PaymentRecord> getPaymentByExternalRef(String botId, String externalRef) {
        String refKey = PAYMENT_REF_KEY_PREFIX + botId + ":" + externalRef;

        return redisManager.get(refKey)
                .flatMap(transactionId -> getPayment(botId, transactionId));
    }

}
