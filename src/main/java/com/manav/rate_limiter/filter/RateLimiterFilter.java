package com.manav.rate_limiter.filter;

import com.manav.rate_limiter.service.RateLimiterService;
import io.github.bucket4j.ConsumptionProbe;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    private final RateLimiterService rateLimiterService;
    private final MeterRegistry meterRegistry;

    private Counter allowedCounter;
    private Counter blockedCounter;
    private Timer latencyTimer;

    @PostConstruct
    void initMetrics() {
        this.allowedCounter = meterRegistry.counter("rate_limiter_allowed_total");
        this.blockedCounter = meterRegistry.counter("rate_limiter_blocked_total");
        this.latencyTimer = meterRegistry.timer("rate_limiter_latency_seconds");
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

       latencyTimer.record(() -> {
            String clientIp = request.getRemoteAddr(); //get the ip from request
            String tier = request.getHeader("X-API-TIER");
            if (tier == null) {
                tier = "FREE";
            }

            String key = tier + ":" + clientIp;

            var bucket = rateLimiterService.resolveBucket(key, tier);
            ConsumptionProbe prob = bucket.tryConsumeAndReturnRemaining(1);

            if (!prob.isConsumed()) {
                blockedCounter.increment();
                response.setStatus(429);
                try {
                    response.getWriter().write("Too many requests for tier"+ tier);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return;
            }

            allowedCounter.increment();
            try {
                filterChain.doFilter(request,response);
            } catch (IOException | ServletException e) {
                throw new RuntimeException(e);
            }
            });
    }
}
