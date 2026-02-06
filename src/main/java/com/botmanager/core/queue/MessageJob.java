package com.botmanager.core.queue;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class MessageJob {

    private String phoneNumberId;

    private String from;

    private String messageId;

    private String messageBody;

    private String messageType;

    private Map<String, Object> raw;

}
