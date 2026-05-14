package com.botmanager.bots.thomasnetwork;

import com.botmanager.core.bot.BaseBot;
import com.botmanager.core.bot.BotConfig;
import com.botmanager.core.flow.FlowEngine;
import com.botmanager.core.flow.FlowPlugin;
import com.botmanager.core.i18n.TranslationService;
import com.botmanager.core.payment.PaymentEventPublisher;
import com.botmanager.core.payment.PaymentGateway;
import com.botmanager.core.payment.PaymentRecord;
import com.botmanager.core.redis.RedisManager;
import com.botmanager.core.whatsapp.WhatsAppClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;

import java.util.UUID;

@Slf4j
public class ThomasNetworkBot extends BaseBot {

    private final ThomasNetworkFlowPlugin plugin;

    public ThomasNetworkBot(BotConfig config,
                           FlowEngine flowEngine,
                           RedisManager redisManager,
                           WhatsAppClientFactory whatsAppClientFactory,
                           ObjectMapper objectMapper,
                           PaymentGateway paymentGateway,
                           TranslationService translationService) {

        super(config, flowEngine, redisManager, whatsAppClientFactory, objectMapper);
        this.plugin = new ThomasNetworkFlowPlugin(paymentGateway, translationService);

        log.info("ThomasNetworkBot initialized: {}", config.getBotId());
    }

    @Override
    public FlowPlugin getPlugin() {
        return plugin;
    }

    public void onPaymentCompleted(PaymentRecord record) {
        if (!config.getBotId().equals(record.getBotId())) {
            return;
        }

        String customerPhone = record.getCustomerPhone();
        String accessCode = generateAccessCode();

        String message = String.format(
                "Payment successful!\n\nYour network access code: %s\n\nThis code is valid for 24 hours.",
                accessCode
        );

        sendMessage(customerPhone, message);

        log.info("Sent access code {} to customer {}", accessCode, customerPhone);
    }

    private String generateAccessCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

}
