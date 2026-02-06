package com.botmanager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.data.redis.url=",
        "mqtt.url="
})
class SpringBotManagerApplicationTest {

    @Test
    void contextLoads() {
    }

}
