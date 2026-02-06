package com.botmanager.core.payment;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class PaymentRequest {

    private String botId;

    private int amount;

    private String currency;

    private String phoneNumber;

    private String reference;

    private String description;

    private Map<String, Object> metadata;

}
