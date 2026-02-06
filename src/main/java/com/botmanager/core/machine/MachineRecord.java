package com.botmanager.core.machine;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class MachineRecord {

    private String botId;

    private String machineId;

    private MachineType type;

    private String name;

    private MachineStatus status;

    private String program;

    private Integer remainingSeconds;

    private String currentUser;

    private Instant lastHeartbeatAt;

    private Instant updatedAt;

}
