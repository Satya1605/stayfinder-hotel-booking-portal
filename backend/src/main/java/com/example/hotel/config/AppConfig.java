package com.example.hotel.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    /** One clock for "today", so tests can replace it with a fixed date. */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
