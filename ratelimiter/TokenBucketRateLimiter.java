package ratelimiter;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * TokenBucketRateLimiter — Production-grade rate limiting implementation.
 *
 * PRODUCTION SCENARIO:
 * ─────────────────────────────────────────────────────────────────────
 * Your payment API is getting hammered. 10,000 req/sec from a single
 * client. DB connections exhausted. Service down for everyone.
 *
 * Root cause: No rate limiting on public-facing endpoints.
 * Fix: Token Bucket rate limiter per client (by API key / IP).
 *
 * HOW TOKEN BUCKET WORKS:
 * ─────────────────────────────────────────────────────────────────────
 *  • Bucket holds N tokens (= max burst capacity)
 *  • Each request consumes 1 token
 *  • Tokens refill at a fixed rate (e.g. 100 tokens/second)
 *  • If bucket is empty → request is REJECTED (429 Too Many Requests)
 *  • Allows short bursts while enforcing sustained rate limits
 *
 * ALTERNATIVES AND WHEN TO USE THEM:
 * ─────────────────────────────────────────────────────────────────────
 *  Fixed Window   → Simple, but allows 2x burst at window boundary
 *  Sliding Window → More accurate, higher memory cost
 *  Token Bucket   → Best for APIs: allows bursts, smooth sustained rate ✓
 *  Leaky Bucket   → Best for queuing/smoothing: processes at fixed rate
 *
 * PRODUCTION NOTES:
 * ─────────────────────────────────────────────────────────────────────
 *  In a distributed system, store token state in Redis (not in-memory).
 *  Use Lua scripts for atomic check-and-decrement in Redis.
 *  Libraries: Resilience4j RateLimiter, Bucket4j (Redis-backed).
 *
 * Author: Mohit Kumar — github.com/Mohit-Java-Caps
 */
public class TokenBucketRateLimiter {

    private final int capacity;           // max tokens (burst limit)
    private final int refillRatePerSec;   // tokens added per second
    private final Map<String, ClientBucket> clientBuckets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService refillScheduler;

    public TokenBucketRateLimiter(int capacity, int refillRatePerSec) {
        this.capacity = capacity;
        this.refillRatePerSec = refillRatePerSec;

        // Background thread refills all client buckets every second
        this.refillScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "rate-limiter-refill");
            t.setDaemon(true);
            return t;
        });

        this.refillScheduler.scheduleAtFixedRate(
            this::refillAllBuckets, 1, 1, TimeUnit.SECONDS
        );
    }

    /**
     * Attempt to consume a token for the given client.
     *
     * @param clientId  API key, user ID, or IP address
     * @return true if allowed, false if rate limited (send 429)
     */
    public boolean tryAcquire(String clientId) {
        ClientBucket bucket = clientBuckets.computeIfAbsent(
            clientId,
            id -> new ClientBucket(capacity) // new clients start with full bucket
        );
        return bucket.tryConsume();
    }

    /**
     * Returns remaining tokens for a client — useful for X-RateLimit-Remaining header.
     */
    public int remainingTokens(String clientId) {
        ClientBucket bucket = clientBuckets.get(clientId);
        return bucket == null ? capacity : bucket.tokens.get();
    }

    private void refillAllBuckets() {
        clientBuckets.values().forEach(bucket -> bucket.refill(refillRatePerSec, capacity));
    }

    public void shutdown() {
        refillScheduler.shutdown();
    }

    // ── Inner class: per-client token bucket ──────────────────────────────
    private static class ClientBucket {
        private final AtomicInteger tokens;

        ClientBucket(int initialTokens) {
            this.tokens = new AtomicInteger(initialTokens);
        }

        boolean tryConsume() {
            // Spin-CAS loop: atomically check-and-decrement
            int current;
            do {
                current = tokens.get();
                if (current <= 0) return false; // bucket empty → reject
            } while (!tokens.compareAndSet(current, current - 1));
            return true;
        }

        void refill(int amount, int max) {
            // Atomically add tokens without exceeding capacity
            tokens.getAndUpdate(current -> Math.min(current + amount, max));
        }
    }

    // ── Demo ──────────────────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {
        // Rate limiter: 5 tokens capacity, refill 2 tokens/second
        TokenBucketRateLimiter limiter = new TokenBucketRateLimiter(5, 2);

        System.out.println("=== Token Bucket Rate Limiter Demo ===");
        System.out.println("Capacity: 5 tokens | Refill: 2 tokens/sec");
        System.out.println("Simulating 8 rapid requests from 'client-A':\n");

        // Rapid burst — should exhaust the bucket
        for (int i = 1; i <= 8; i++) {
            boolean allowed = limiter.tryAcquire("client-A");
            System.out.printf("  Request %d: %-10s | Remaining tokens: %d%n",
                i,
                allowed ? "ALLOWED ✓" : "REJECTED ✗ (429)",
                limiter.remainingTokens("client-A")
            );
        }

        System.out.println("\n  Waiting 2 seconds for token refill...\n");
        Thread.sleep(2100);

        System.out.println("  After refill — 2 new requests:");
        for (int i = 1; i <= 2; i++) {
            boolean allowed = limiter.tryAcquire("client-A");
            System.out.printf("  Request %d: %-10s | Remaining tokens: %d%n",
                i,
                allowed ? "ALLOWED ✓" : "REJECTED ✗ (429)",
                limiter.remainingTokens("client-A")
            );
        }

        System.out.println("\n  Testing second client 'client-B' (independent bucket):");
        boolean allowed = limiter.tryAcquire("client-B");
        System.out.printf("  client-B Request 1: %s | Remaining: %d%n",
            allowed ? "ALLOWED ✓" : "REJECTED ✗",
            limiter.remainingTokens("client-B")
        );

        System.out.println("\nProduction note: Use Redis + Lua for distributed rate limiting.");
        limiter.shutdown();
    }
}
