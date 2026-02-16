# Distributed Rate Limiter Gateway (Spring Boot)

## Overview
A distributed API rate-limiting gateway built with Spring Boot and Bucket4j, using Redis for shared state and Prometheus + Grafana for real-time observability. Supports tier-based throttling (FREE/PREMIUM) and is fully Dockerized for local infrastructure.

## Architecture
Client → RateLimiterFilter → Bucket4j (Token Bucket) → Redis  
                             ↘ Metrics → Prometheus → Grafana

## Tech Stack
- Java 21, Spring Boot
- Bucket4j (Token Bucket rate limiting)
- Redis (distributed state)
- Micrometer + Prometheus (metrics)
- Grafana (dashboards)
- Docker Compose (infra)

## Features
- Tier-based limits (FREE vs PREMIUM) via request header `X-API-TIER`
- Redis-backed token buckets for distributed rate limiting
- Real-time metrics for allowed/blocked requests and latency
- Grafana dashboard for traffic visualization

## Configuration
Rate limits are configured in `src/main/resources/application.yml`:
```yaml
rate-limiter:
  free:
    capacity: 4
    per-minute: 4
  premium:
    capacity: 100
    per-minute: 100
```
---

## How to Run (Local)

### 1. Start Infrastructure (Redis + Prometheus + Grafana)
```bash
docker compose up -d
```
Verify:
	•	Prometheus: http://localhost:9090
	•	Grafana: http://localhost:3000 (login: admin / admin)

 ## 2. Start Spring Boot App
```bash
mvn spring-boot:run
```
Health check:
```bash
curl http://localhost:8080/api/ping
```
Metrics endpoint:
```bash
http://localhost:8080/actuator/prometheus
```

## How to Test Rate Limiting
FREE Tier (default)
```bash
for i in {1..10}; do curl -i http://localhost:8080/api/ping; done

Expected: blocked after configured FREE limit (HTTP 429).
```
PREMIUM Tier
```bash
for i in {1..120}; do curl -i -H "X-API-TIER: PREMIUM" http://localhost:8080/api/ping; done

Expected: blocked after configured PREMIUM limit.
```

## Monitoring & Dashboards
Prometheus
	•	URL: http://localhost:9090
	•	Targets: http://localhost:9090/targets (should show app as UP)

Grafana
	•	URL: http://localhost:3000
	•	Login: admin / admin
	•	Data source: Prometheus (http://prometheus:9090)

 ## Reset / Restart (Local)

 ```bash
1) docker compose down -v
   docker compose up -d

2) mvn clean spring-boot:run
```

