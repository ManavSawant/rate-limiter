package com.manav.rate_limiter.service;

import com.manav.rate_limiter.config.RateLimiterProperties;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final ProxyManager<byte[]> proxyManager;
    private final RateLimiterProperties properties;

    public Bucket resolveBucket(String key, String tier) {
        RateLimiterProperties.Tier cfg =
                "PREMIUM".equalsIgnoreCase(tier)
                ? properties.getPremium()
                : properties.getFree();

        Bandwidth limit = Bandwidth.builder()
                .capacity(cfg.getCapacity())
                .refillIntervally(cfg.getPerMinute(), Duration.ofMinutes(1))
                .build();

        return proxyManager.builder()
                .build(key.getBytes(StandardCharsets.UTF_8),
                        ()-> BucketConfiguration.builder()
                                .addLimit(limit)
                                .build());
    }

}
