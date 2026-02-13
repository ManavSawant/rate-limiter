package com.manav.rate_limiter.service;

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

    public Bucket resolveBucket(String key, String tier) {
        Bandwidth limit;

        if("PREMIUM".equalsIgnoreCase(tier)){
            limit = Bandwidth.builder()
                    .capacity(100)
                    .refillIntervally(100, Duration.ofMinutes(1))
                    .build();
        }else{
            limit = Bandwidth.builder()
                    .capacity(10)
                    .refillIntervally(10, Duration.ofMinutes(1))
                    .build();
        }
        return proxyManager.builder()
                .build(key.getBytes(StandardCharsets.UTF_8), () -> BucketConfiguration.builder()
                        .addLimit(limit)
                        .build());
    }

}
