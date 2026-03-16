package com.botmanager.admin;

import org.springframework.context.ApplicationEvent;

public class BotRegistryRefreshEvent extends ApplicationEvent {

    public BotRegistryRefreshEvent(Object source) {
        super(source);
    }
}