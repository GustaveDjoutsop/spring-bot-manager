package com.botmanager.core.bot;

import com.botmanager.core.flow.FlowDefinition;
import com.botmanager.core.machine.MachineConfig;
import com.botmanager.core.machine.ProgramConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class BotConfig {

    private String botId;

    private String botName;

    private String botType;

    private String phoneNumberId;

    private String verifyToken;

    private MqttConfig mqtt;

    private List<MachineConfig> machines;

    private Map<String, List<ProgramConfig>> programs;

    private String defaultFlowId;

    private Map<String, FlowDefinition> flows;

    @Getter
    @Setter
    public static class MqttConfig {

        private String topicPrefix;
    }

}
