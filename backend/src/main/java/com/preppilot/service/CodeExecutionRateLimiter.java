package com.preppilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple process-local token bucket limiter for ad-hoc Judge0 executions. */
@Service
public class CodeExecutionRateLimiter {

    private final double capacity;
    private final double refillTokensPerNano;
    private final Clock clock;
    private final Map<String, Bucket> bucketsByUser = new ConcurrentHashMap<>();

    public CodeExecutionRateLimiter(
            @Value("${app.rate-limit.code-run.max-requests:10}") int maxRequests,
            @Value("${app.rate-limit.code-run.window-seconds:60}") long windowSeconds) {
        this(maxRequests, Duration.ofSeconds(windowSeconds), Clock.systemUTC());
    }

    CodeExecutionRateLimiter(int maxRequests, Duration window, Clock clock) {
        if (maxRequests < 1 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate-limit values must be positive");
        }
        this.capacity = maxRequests;
        this.refillTokensPerNano = (double) maxRequests / window.toNanos();
        this.clock = clock;
    }

    public boolean tryAcquire(String userId) {
        long nowNanos = clock.instant().toEpochMilli() * 1_000_000L;
        Bucket bucket = bucketsByUser.computeIfAbsent(userId, ignored -> new Bucket(capacity, nowNanos));
        synchronized (bucket) {
            long elapsedNanos = Math.max(0L, nowNanos - bucket.lastRefillNanos);
            bucket.tokens = Math.min(capacity, bucket.tokens + elapsedNanos * refillTokensPerNano);
            bucket.lastRefillNanos = nowNanos;
            if (bucket.tokens < 1.0d) {
                return false;
            }
            bucket.tokens -= 1.0d;
            return true;
        }
    }

    private static final class Bucket {
        private double tokens;
        private long lastRefillNanos;

        private Bucket(double tokens, long lastRefillNanos) {
            this.tokens = tokens;
            this.lastRefillNanos = lastRefillNanos;
        }
    }
}
