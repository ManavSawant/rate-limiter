package com.manav.rate_limiter.config;

import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BucketConfig {

    @Bean
    public RedisClient redisClient() {
        return RedisClient.create(RedisURI.Builder.redis("localHost", 6379).build());
    }

    @Bean
    public ProxyManager<byte[]> proxyManager(RedisClient redisClient) {
        return LettuceBasedProxyManager.builderFor(redisClient).build();
    }

}
