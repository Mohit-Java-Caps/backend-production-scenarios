package idempotency;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IdempotencyHandler — Prevents duplicate operations in distributed systems.
 *
 * PRODUCTION SCENARIO:
 * ─────────────────────────────────────────────────────────────────────
 * A customer clicks "Pay" once. Network hiccup. Browser retries.
 * Payment service receives the request TWICE.
 * Customer is charged TWICE.
 * Nightmare.
 *
 * Idempotency keys solve this:
 *   → Client generates a unique key per logical operation
 *   → Server stores: key → result
 *   → If same key arrives again → return cached result, don't re-execute
 *   → Customer is charged exactly once, no matter how many retries
 *
 * WHERE THIS PATTERN IS USED:
 * ─────────────────────────────────────────────────────────────────────
 *  Stripe API    → X-Idempotency-Key header on every payment endpoint
 *  PayPal API    → PayPal-Request-Id header
 *  AWS SDK       → ClientToken on mutation operations
 *  Kafka         → exactly-once semantics via producer ID + sequence
 *
 * PRODUCTION IMPLEMENTATION:
 * ─────────────────────────────────────────────────────────────────────
 *  1. Store keys in Redis with TTL (24h is Stripe's default)
 *  2. Use atomic SET NX (set if not exists) to prevent race conditions
 *  3. Store full response (status + body), not just a flag
 *  4. Return 409 Conflict if same key arrives while still processing
 *
 * Author: Mohit Kumar — github.com/Mohit-Java-Caps
 */
public class IdempotencyHandler {

    public enum Status { PROCESSING, COMPLETED, FAILED }

    /**
     * Stored result for an idempotency key.
     */
    public record IdempotentResult(
        String idempotencyKey,
        Status status,
        Object response,
        String errorMessage,
        long timestampMs
    ) {}

    // In production: replace with Redis with TTL
    private final Map<String, IdempotentResult> store = new ConcurrentHashMap<>();

    /**
     * Execute an operation with idempotency guarantee.
     *
     * @param idempotencyKey  unique key for this logical operation (UUID from client)
     * @param operation       the actual business logic to execute once
     * @return the result — either freshly computed or cached from previous execution
     */
    public IdempotentResult execute(String idempotencyKey, java.util.function.Supplier<Object> operation) {
        // Check if we've seen this key before
        IdempotentResult existing = store.get(idempotencyKey);

        if (existing != null) {
            if (existing.status() == Status.PROCESSING) {
                // Concurrent duplicate — still processing
                System.out.println("  [IDEMPOTENCY] Key " + idempotencyKey + " still PROCESSING — return 409");
                return existing;
            }
            // Already completed — return cached result (no re-execution)
            System.out.println("  [IDEMPOTENCY] Key " + idempotencyKey + " already COMPLETED — returning cached result");
            return existing;
        }

        // Mark as PROCESSING (atomic in Redis: SET NX)
        IdempotentResult processing = new IdempotentResult(
            idempotencyKey, Status.PROCESSING, null, null, System.currentTimeMillis()
        );
        store.put(idempotencyKey, processing);

        // Execute the real operation
        try {
            Object result = operation.get();
            IdempotentResult completed = new IdempotentResult(
                idempotencyKey, Status.COMPLETED, result, null, System.currentTimeMillis()
            );
            store.put(idempotencyKey, completed);
            System.out.println("  [IDEMPOTENCY] Key " + idempotencyKey + " COMPLETED — result stored");
            return completed;

        } catch (Exception e) {
            IdempotentResult failed = new IdempotentResult(
                idempotencyKey, Status.FAILED, null, e.getMessage(), System.currentTimeMillis()
            );
            store.put(idempotencyKey, failed);
            System.out.println("  [IDEMPOTENCY] Key " + idempotencyKey + " FAILED — error stored");
            return failed;
        }
    }

    public boolean hasKey(String key) { return store.containsKey(key); }
    public int storeSize()            { return store.size(); }

    // ── Demo ──────────────────────────────────────────────────────────────
    public static void main(String[] args) {
        IdempotencyHandler handler = new IdempotencyHandler();

        System.out.println("=== Idempotency Key Demo ===");
        System.out.println("Scenario: Customer clicks Pay, network retries 3 times\n");

        // Client generates ONE idempotency key per logical payment
        String idempotencyKey = UUID.randomUUID().toString();
        System.out.println("Client generated idempotency key: " + idempotencyKey);
        System.out.println();

        // Simulate the payment operation
        java.util.function.Supplier<Object> paymentOp = () -> {
            System.out.println("  [PAYMENT SERVICE] Processing payment of $149.99...");
            // Simulate some processing time
            try { Thread.sleep(50); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            return Map.of(
                "transactionId", "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "amount",        "$149.99",
                "status",        "CHARGED",
                "timestamp",     System.currentTimeMillis()
            );
        };

        // Attempt 1 — real execution
        System.out.println("--- Attempt 1 (original request) ---");
        IdempotentResult result1 = handler.execute(idempotencyKey, paymentOp);
        System.out.println("  Response: " + result1.response());
        System.out.println("  Status: " + result1.status());

        System.out.println();

        // Attempt 2 — network retry (same key)
        System.out.println("--- Attempt 2 (network retry — same key) ---");
        IdempotentResult result2 = handler.execute(idempotencyKey, paymentOp);
        System.out.println("  Response: " + result2.response());
        System.out.println("  Status: " + result2.status());

        System.out.println();

        // Attempt 3 — browser retry (same key again)
        System.out.println("--- Attempt 3 (browser retry — same key again) ---");
        IdempotentResult result3 = handler.execute(idempotencyKey, paymentOp);
        System.out.println("  Response: " + result3.response());
        System.out.println("  Status: " + result3.status());

        System.out.println();
        System.out.println("=== Result ===");
        System.out.println("Payment operation executed: 1 time (not 3)");
        System.out.println("Customer charged: ONCE");
        System.out.println("Keys in store: " + handler.storeSize());
        System.out.println("\nProduction: Store keys in Redis with 24h TTL. Use SET NX for atomicity.");
    }
}
