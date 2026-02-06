package com.manav.rate_limiter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr(); //get the ip from request
        String key = "rate:" + clientIp;

        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1){
            redisTemplate.expire(key, WINDOW);
        }
        if (count != null && count > MAX_REQUESTS){
            response.setStatus(429);
            response.getWriter().write("too many requests");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
