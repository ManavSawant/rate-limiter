package com.manav.rate_limiter.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "rate-limiter")
public class RateLimiterProperties {

    private Tier free = new Tier();
    private Tier premium = new Tier();

    @Data
    public static class Tier {
        private int capacity;
        private int perMinute;
    }
}