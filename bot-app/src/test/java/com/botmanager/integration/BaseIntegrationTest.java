package com.botmanager.integration;

import com.botmanager.core.bot.BotRegistryRefreshEvent;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("integration")
public abstract class BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @BeforeEach
    void resetDatabase() {
        jdbcTemplate.execute("TRUNCATE TABLE pharmacy_reservations, pharmacy_products, payments, messages, businesses RESTART IDENTITY CASCADE");
        new ResourceDatabasePopulator(new ClassPathResource("db/migration/V4__seed_existing_bots.sql"))
                .execute(jdbcTemplate.getDataSource());
        eventPublisher.publishEvent(new BotRegistryRefreshEvent(this));
    }

}
