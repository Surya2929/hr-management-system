package com.hrapp.config;

import com.hrapp.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Very small in-memory rate limiter (no extra library).
 * Each key (e.g. "login:email:a@b.com") may be used maxAttempts times per time window.
 * Stops password guessing on login / activation / forgot-password.
 * Note: counters live in this server's memory, so they reset when the server restarts.
 */
@Component
public class RateLimiter {

    // One counting window per key
    private static class Window {
        Instant start;
        int count;
        Window(Instant start) { this.start = start; }
    }

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    // Counts one attempt; throws 429 if the key is already over the limit
    public void check(String key, int maxAttempts, Duration window) {
        Instant now = Instant.now();
        cleanUpIfLarge(now, window);

        Window w = windows.compute(key, (k, old) -> {
            if (old == null || old.start.plus(window).isBefore(now)) {
                return new Window(now); // first attempt, or the old window is over
            }
            return old;
        });

        synchronized (w) {
            w.count++;
            if (w.count > maxAttempts) {
                long minutesLeft = Math.max(1, Duration.between(now, w.start.plus(window)).toMinutes() + 1);
                throw new TooManyRequestsException(
                        "Too many attempts. Please try again in " + minutesLeft + " minute(s).");
            }
        }
    }

    // Forget a key (e.g. after a successful login, so earlier typos don't count)
    public void reset(String key) {
        windows.remove(key);
    }

    // The visitor's IP. On Render the real IP is the first entry of X-Forwarded-For.
    public static String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    // Keep memory small: drop finished windows when the map grows big
    private void cleanUpIfLarge(Instant now, Duration window) {
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(e -> e.getValue().start.plus(window).isBefore(now));
        }
    }
}
