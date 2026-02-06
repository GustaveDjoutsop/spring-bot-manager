package com.botmanager.bots.laundry;

import com.botmanager.core.bot.BotConfig;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class LaundryBotConfig extends BotConfig {

    private CycleConfig shortCycle = new CycleConfig(30, 1000, 1);

    private CycleConfig longCycle = new CycleConfig(60, 2000, 2);

    private BusinessHoursConfig businessHours = new BusinessHoursConfig();

    private List<String> availableMachineIds;

    private String staffAlertPhone;

    @Getter
    @Setter
    public static class BusinessHoursConfig {

        private String openTime = "07:00";

        private String closeTime = "22:00";

        private int closingBufferMinutes = 15;

        private String timezone = "Africa/Douala";
    }

}
