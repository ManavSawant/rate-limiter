package com.manav.rate_limiter.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class TestController {

    private final StringRedisTemplate stringRedisTemplate;

    @GetMapping("/ping")
    public String ping(){
        return "OK";
    }

    @GetMapping("/redis-test")
    public String redisTest(){
        stringRedisTemplate.opsForValue().set("test", "ok");
        return stringRedisTemplate.opsForValue().get("test");
    }
}
