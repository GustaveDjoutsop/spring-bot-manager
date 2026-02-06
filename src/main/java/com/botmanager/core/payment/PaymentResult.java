package com.botmanager.core.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class PaymentResult {

    private boolean success;

    private String transactionId;

    private String externalRef;

    private PaymentStatus status;

    private String errorMessage;

    private Map<String, Object> raw;

}
