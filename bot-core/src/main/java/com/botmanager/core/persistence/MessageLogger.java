package com.botmanager.core.persistence;

import com.botmanager.core.persistence.entity.BusinessEntity;
import com.botmanager.core.persistence.entity.MessageEntity;
import com.botmanager.core.persistence.repository.BusinessRepository;
import com.botmanager.core.persistence.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageLogger {

    private final MessageRepository messageRepository;

    private final BusinessRepository businessRepository;

    @Async
    public void logInbound(String botId, String senderPhone, String messageType, String content, String whatsappMsgId) {
        persistMessage(botId, senderPhone, "INBOUND", messageType, content, whatsappMsgId);
    }

    @Async
    public void logOutbound(String botId, String recipientPhone, String messageType, String content) {
        persistMessage(botId, recipientPhone, "OUTBOUND", messageType, content, null);
    }

    private void persistMessage(String botId, String phone, String direction, String messageType, String content, String whatsappMsgId) {
        try {
            BusinessEntity business = businessRepository.findByBotId(botId).orElse(null);

            MessageEntity entity = new MessageEntity();
            entity.setBusiness(business);
            entity.setSenderPhone(phone);
            entity.setDirection(direction);
            entity.setMessageType(messageType);
            entity.setContent(content);
            entity.setWhatsappMsgId(whatsappMsgId);

            messageRepository.save(entity);
        } catch (Exception exception) {
            log.warn("Failed to log {} message for bot {}: {}", direction, botId, exception.getMessage());
        }
    }
}
