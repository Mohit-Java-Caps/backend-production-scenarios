package retry;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * ExponentialBackoffRetry — Production-grade retry with jitter.
 *
 * PRODUCTION SCENARIO:
 * ─────────────────────────────────────────────────────────────────────
 * Your service calls an external API. It fails occasionally due to
 * transient errors (network blip, temporary overload, GC pause).
 * Without retry: 0.1% failure rate becomes a customer-visible error.
 * With naive retry (immediate): thundering herd — all retries hit at once.
 * With exponential backoff + jitter: retries spread out, system recovers.
 *
 * EXPONENTIAL BACKOFF:
 *   Attempt 1: wait 1s  → retry
 *   Attempt 2: wait 2s  → retry
 *   Attempt 3: wait 4s  → retry
 *   Attempt 4: wait 8s  → give up (max retries reached)
 *
 * WHY JITTER (randomness):
 * ─────────────────────────────────────────────────────────────────────
 *  Without jitter: 1000 clients all retry at t=2s → thundering herd.
 *  With jitter: retries spread across [0, 2s] → smooth recovery.
 *  AWS, Google Cloud, Stripe all use jitter in their client SDKs.
 *
 * WHAT TO RETRY (and what NOT to):
 * ─────────────────────────────────────────────────────────────────────
 *  ✅ Retry: 429 Too Many Requests, 503 Service Unavailable, timeouts
 *  ❌ Don't retry: 400 Bad Request, 401 Unauthorized, 404 Not Found
 *     (these are client errors — retrying won't help and wastes resources)
 *
 * Author: Mohit Kumar — github.com/Mohit-Java-Caps
 */
public class ExponentialBackoffRetry {

    private final int     maxAttempts;
    private final long    initialDelayMs;
    private final long    maxDelayMs;
    private final double  multiplier;
    private final boolean useJitter;

    public ExponentialBackoffRetry(int maxAttempts, long initialDelayMs,
                                   long maxDelayMs, double multiplier, boolean useJitter) {
        this.maxAttempts    = maxAttempts;
        this.initialDelayMs = initialDelayMs;
        this.maxDelayMs     = maxDelayMs;
        this.multiplier     = multiplier;
        this.useJitter      = useJitter;
    }

    /**
     * Execute a supplier with automatic retry on exception.
     *
     * @param supplier     the operation to attempt (e.g. HTTP call, DB query)
     * @param retryOn      exception types that should trigger a retry
     * @return result on success
     * @throws RuntimeException if all attempts exhausted
     */
    @SafeVarargs
    public final <T> T execute(Supplier<T> supplier,
                               Class<? extends Exception>... retryOn) {
        long delay = initialDelayMs;
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                T result = supplier.get();
                if (attempt > 1) {
                    System.out.printf("  ✓ Succeeded on attempt %d%n", attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;

                if (!isRetryable(e, retryOn)) {
                    System.out.printf("  ✗ Non-retryable error: %s. Giving up.%n", e.getMessage());
                    throw new RuntimeException("Non-retryable failure", e);
                }

                if (attempt == maxAttempts) {
                    System.out.printf("  ✗ All %d attempts exhausted. Last error: %s%n",
                        maxAttempts, e.getMessage());
                    break;
                }

                long actualDelay = computeDelay(delay);
                System.out.printf("  ✗ Attempt %d failed: %s. Retrying in %dms...%n",
                    attempt, e.getMessage(), actualDelay);

                sleep(actualDelay);
                delay = Math.min((long)(delay * multiplier), maxDelayMs);
            }
        }

        throw new RuntimeException("All retry attempts exhausted", lastException);
    }

    private long computeDelay(long baseDelay) {
        if (!useJitter) return baseDelay;
        // Full jitter: random delay between [0, baseDelay]
        // Spreads retries across the window — prevents thundering herd
        return ThreadLocalRandom.current().nextLong(0, baseDelay + 1);
    }

    @SafeVarargs
    private boolean isRetryable(Exception e, Class<? extends Exception>... retryOn) {
        if (retryOn.length == 0) return true; // retry all by default
        for (Class<? extends Exception> type : retryOn) {
            if (type.isInstance(e)) return true;
        }
        return false;
    }

    private void sleep(long ms) {
        if (ms <= 0) return;
        try { Thread.sleep(ms); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ── Demo ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("=== Exponential Backoff Retry Demo ===\n");

        ExponentialBackoffRetry retry = new ExponentialBackoffRetry(
            4,      // max 4 attempts
            200,    // start at 200ms
            3000,   // cap at 3 seconds
            2.0,    // double each time
            true    // use jitter
        );

        // Scenario 1: Service recovers on 3rd attempt
        System.out.println("Scenario 1: Service fails twice, succeeds on attempt 3");
        int[] callCount = {0};
        try {
            String result = retry.execute(() -> {
                callCount[0]++;
                if (callCount[0] < 3) {
                    throw new RuntimeException("503 Service Unavailable");
                }
                return "Order created: ORD-" + System.currentTimeMillis();
            }, RuntimeException.class);
            System.out.println("  Result: " + result);
        } catch (Exception e) {
            System.out.println("  Failed: " + e.getMessage());
        }

        System.out.println();

        // Scenario 2: All retries exhausted
        System.out.println("Scenario 2: Service never recovers — all attempts fail");
        try {
            retry.execute(
                () -> { throw new RuntimeException("503 Service Unavailable"); },
                RuntimeException.class
            );
        } catch (Exception e) {
            System.out.println("  Final result: Gave up after all attempts.");
        }

        System.out.println();

        // Scenario 3: Non-retryable error
        System.out.println("Scenario 3: Non-retryable error (400 Bad Request)");
        try {
            retry.execute(
                () -> { throw new IllegalArgumentException("400 Bad Request: invalid userId"); },
                RuntimeException.class // IllegalArgumentException is not RuntimeException — not retried
            );
        } catch (Exception e) {
            System.out.println("  Correctly not retried.");
        }

        System.out.println("\nProduction: Use Resilience4j Retry — handles thread safety,");
        System.out.println("metrics, events, and Spring Boot integration out of the box.");
    }
}
