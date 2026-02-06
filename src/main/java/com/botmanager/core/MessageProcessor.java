package com.botmanager.core;

import com.botmanager.core.bot.BotRegistry;
import com.botmanager.core.queue.MessageJob;
import com.botmanager.core.queue.QueueManager;
import com.botmanager.core.redis.RedisManager;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageProcessor {

    private static final long LOCK_TTL_SECONDS = 60;

    private final QueueManager queueManager;

    private final BotRegistry botRegistry;

    private final RedisManager redisManager;

    @PostConstruct
    void init() {
        queueManager.setProcessor(this::processMessage);
        log.info("Message processor initialized");
    }

    private void processMessage(MessageJob job) {
        String phoneNumberId = job.getPhoneNumberId();
        String from = job.getFrom();
        String messageId = job.getMessageId();

        botRegistry.getBotByPhoneId(phoneNumberId).ifPresentOrElse(
                bot -> {
                    String lockKey = "lock:" + bot.getConfig().getBotId() + ":" + from + ":" + messageId;

                    boolean acquired = redisManager.setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS);
                    if (!acquired) {
                        log.debug("Duplicate message {} from {}, skipping", messageId, from);

                        return;
                    }

                    try {
                        bot.handleMessage(job);
                    } catch (Exception exception) {
                        log.error("Failed to handle message {} from {}: {}",
                                messageId, from, exception.getMessage());
                    }
                },
                () -> log.warn("No bot registered for phoneNumberId: {}", phoneNumberId)
        );
    }

}
