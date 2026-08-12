package com.preppilot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Simple process-local sliding-window limiter for ad-hoc Judge0 executions. */
@Service
public class CodeExecutionRateLimiter {

    private final int maxRequests;
    private final Duration window;
    private final Clock clock;
    private final Map<String, Deque<Instant>> requestsByUser = new ConcurrentHashMap<>();

    public CodeExecutionRateLimiter(
            @Value("${app.rate-limit.code-run.max-requests:10}") int maxRequests,
            @Value("${app.rate-limit.code-run.window-seconds:60}") long windowSeconds) {
        this(maxRequests, Duration.ofSeconds(windowSeconds), Clock.systemUTC());
    }

    CodeExecutionRateLimiter(int maxRequests, Duration window, Clock clock) {
        if (maxRequests < 1 || window.isZero() || window.isNegative()) {
            throw new IllegalArgumentException("Rate-limit values must be positive");
        }
        this.maxRequests = maxRequests;
        this.window = window;
        this.clock = clock;
    }

    public boolean tryAcquire(String userId) {
        Instant now = clock.instant();
        Deque<Instant> requests = requestsByUser.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        synchronized (requests) {
            Instant cutoff = now.minus(window);
            while (!requests.isEmpty() && requests.peekFirst().isBefore(cutoff)) {
                requests.removeFirst();
            }
            if (requests.size() >= maxRequests) {
                return false;
            }
            requests.addLast(now);
            return true;
        }
    }
}
