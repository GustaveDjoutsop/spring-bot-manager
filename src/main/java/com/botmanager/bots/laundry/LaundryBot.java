package com.botmanager.bots.laundry;

import com.botmanager.core.bot.BaseBot;
import com.botmanager.core.flow.FlowEngine;
import com.botmanager.core.flow.FlowPlugin;
import com.botmanager.core.i18n.TranslationService;
import com.botmanager.core.machine.MachineService;
import com.botmanager.core.payment.PaymentGateway;
import com.botmanager.core.redis.RedisManager;
import com.botmanager.core.whatsapp.WhatsAppClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LaundryBot extends BaseBot {

    private final LaundryFlowPlugin plugin;

    public LaundryBot(LaundryBotConfig config,
                      FlowEngine flowEngine,
                      RedisManager redisManager,
                      WhatsAppClientFactory whatsAppClientFactory,
                      ObjectMapper objectMapper,
                      PaymentGateway paymentGateway,
                      MachineService machineService,
                      TranslationService translationService) {

        super(config, flowEngine, redisManager, whatsAppClientFactory, objectMapper);
        this.plugin = new LaundryFlowPlugin(paymentGateway, machineService, translationService, config);

        log.info("LaundryBot initialized: {}", config.getBotId());
    }

    @Override
    public FlowPlugin getPlugin() {
        return plugin;
    }

}
