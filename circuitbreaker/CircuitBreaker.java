package circuitbreaker;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * CircuitBreaker — Hand-rolled implementation of the Circuit Breaker pattern.
 *
 * PRODUCTION SCENARIO:
 * ─────────────────────────────────────────────────────────────────────
 * Your Order service calls Payment service. Payment service goes down.
 * Without a circuit breaker:
 *   → Every order request hangs for 30s (connection timeout)
 *   → Thread pool fills up waiting for Payment service
 *   → Order service also crashes (cascading failure)
 *   → Your entire platform is down
 *
 * With a circuit breaker:
 *   → After N failures, circuit OPENS
 *   → Requests fail fast (no hanging) → system stays responsive
 *   → After a cooldown, circuit tries again (HALF-OPEN)
 *   → If successful → circuit CLOSES → normal operation resumes
 *
 * THREE STATES:
 * ─────────────────────────────────────────────────────────────────────
 *  CLOSED     → Normal operation. Failures are counted.
 *               If failures >= threshold → trip to OPEN.
 *
 *  OPEN       → Fast fail. No requests pass through.
 *               After cooldown period → move to HALF_OPEN.
 *
 *  HALF_OPEN  → Trial mode. Let ONE request through.
 *               Success → CLOSE (recovered).
 *               Failure → back to OPEN (still broken).
 *
 * PRODUCTION TOOLS: Resilience4j CircuitBreaker, Spring Cloud CircuitBreaker.
 *
 * Author: Mohit Kumar — github.com/Mohit-Java-Caps
 */
public class CircuitBreaker {

    public enum State { CLOSED, OPEN, HALF_OPEN }

    private final String name;
    private final int failureThreshold;     // failures before opening
    private final int successThreshold;     // successes in HALF_OPEN to close
    private final long cooldownMillis;      // time in OPEN before trying again

    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    private final AtomicInteger failureCount  = new AtomicInteger(0);
    private final AtomicInteger successCount  = new AtomicInteger(0);
    private final AtomicLong   lastFailedTime = new AtomicLong(0);

    public CircuitBreaker(String name, int failureThreshold, int successThreshold, long cooldownMillis) {
        this.name             = name;
        this.failureThreshold = failureThreshold;
        this.successThreshold = successThreshold;
        this.cooldownMillis   = cooldownMillis;
    }

    /**
     * Execute a supplier through the circuit breaker.
     * Throws CircuitOpenException immediately if circuit is OPEN.
     *
     * @param supplier  the remote call to protect (e.g. HTTP call, DB query)
     * @return result of the supplier
     */
    public <T> T execute(Supplier<T> supplier) {
        State current = currentState();

        if (current == State.OPEN) {
            throw new CircuitOpenException(
                "Circuit breaker [" + name + "] is OPEN. Fast-failing to protect system."
            );
        }

        try {
            T result = supplier.get();
            onSuccess();
            return result;
        } catch (Exception e) {
            onFailure();
            throw e;
        }
    }

    /**
     * Returns the effective current state, transitioning OPEN → HALF_OPEN if cooldown elapsed.
     */
    private State currentState() {
        if (state.get() == State.OPEN) {
            long elapsed = System.currentTimeMillis() - lastFailedTime.get();
            if (elapsed >= cooldownMillis) {
                // Cooldown passed — attempt recovery
                state.compareAndSet(State.OPEN, State.HALF_OPEN);
                System.out.printf("[%s] Cooldown elapsed. Transitioning OPEN → HALF_OPEN (trial mode)%n", name);
            }
        }
        return state.get();
    }

    private void onSuccess() {
        State current = state.get();
        if (current == State.HALF_OPEN) {
            int successes = successCount.incrementAndGet();
            System.out.printf("[%s] HALF_OPEN success %d/%d%n", name, successes, successThreshold);
            if (successes >= successThreshold) {
                reset();
                System.out.printf("[%s] Recovered! Transitioning HALF_OPEN → CLOSED%n", name);
            }
        } else if (current == State.CLOSED) {
            // Success in CLOSED — reset failure count
            failureCount.set(0);
        }
    }

    private void onFailure() {
        lastFailedTime.set(System.currentTimeMillis());
        State current = state.get();

        if (current == State.HALF_OPEN) {
            // Trial failed — go back to OPEN
            state.set(State.OPEN);
            successCount.set(0);
            System.out.printf("[%s] HALF_OPEN trial failed. Back to OPEN.%n", name);
            return;
        }

        int failures = failureCount.incrementAndGet();
        System.out.printf("[%s] Failure %d/%d recorded%n", name, failures, failureThreshold);

        if (failures >= failureThreshold) {
            state.set(State.OPEN);
            System.out.printf("[%s] Threshold reached! Transitioning CLOSED → OPEN%n", name);
        }
    }

    private void reset() {
        state.set(State.CLOSED);
        failureCount.set(0);
        successCount.set(0);
    }

    public State getState()    { return state.get(); }
    public int   getFailures() { return failureCount.get(); }

    public static class CircuitOpenException extends RuntimeException {
        public CircuitOpenException(String msg) { super(msg); }
    }

    // ── Demo ──────────────────────────────────────────────────────────────
    public static void main(String[] args) throws InterruptedException {
        CircuitBreaker cb = new CircuitBreaker("PaymentService", 3, 2, 2000);

        System.out.println("=== Circuit Breaker Demo ===\n");
        System.out.println("Settings: Open after 3 failures | Close after 2 successes | Cooldown: 2s\n");

        // Simulate a failing remote service
        Supplier<String> failingService = () -> {
            throw new RuntimeException("Connection refused: payment-service:8080");
        };

        Supplier<String> workingService = () -> "Payment processed: TXN-" + System.currentTimeMillis();

        // Phase 1: Failures trip the breaker
        System.out.println("--- Phase 1: Simulating failures ---");
        for (int i = 1; i <= 4; i++) {
            try {
                cb.execute(failingService);
            } catch (CircuitOpenException e) {
                System.out.println("  Fast-fail: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("  Service error: " + e.getMessage());
            }
            System.out.println("  Circuit state: " + cb.getState());
            System.out.println();
        }

        // Phase 2: Cooldown
        System.out.println("--- Phase 2: Waiting 2 seconds cooldown ---");
        Thread.sleep(2100);

        // Phase 3: Half-open trial
        System.out.println("\n--- Phase 3: Trial requests (HALF_OPEN) ---");
        for (int i = 1; i <= 3; i++) {
            try {
                String result = cb.execute(workingService);
                System.out.println("  Success: " + result);
            } catch (CircuitOpenException e) {
                System.out.println("  Fast-fail: " + e.getMessage());
            }
            System.out.println("  Circuit state: " + cb.getState());
            System.out.println();
        }

        System.out.println("Final state: " + cb.getState());
        System.out.println("\nProduction: Use Resilience4j — fully battle-tested with Spring Boot integration.");
    }
}
