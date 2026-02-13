package com.manav.rate_limiter.filter;

import com.manav.rate_limiter.service.RateLimiterService;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.util.RateLimiter;
import org.jspecify.annotations.NonNull;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;

    private static final int MAX_REQUESTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(1);


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = request.getRemoteAddr(); //get the ip from request
        String tier = request.getHeader("X-API-TIER");
        if (tier == null){
            tier = "FREE";
        }

        String key = tier + ":" + clientIp;

        var bucket = rateLimiterService.resolveBucket(key,tier);
        ConsumptionProbe prob = bucket.tryConsumeAndReturnRemaining(1);

        if (!prob.isConsumed()) {
            response.setStatus(429);
            response.getWriter().write("Too many requests for tier" + tier);
            return;
        }
        filterChain.doFilter(request, response);
    }
}
