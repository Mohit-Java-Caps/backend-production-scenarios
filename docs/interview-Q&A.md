
# 🔍 Backend Production Scenarios – Interview Q\&A

*(Scenario-Based – Spoken Style)*

***

## 1️⃣ API suddenly becomes slow after deployment. How do you debug?

**Answer:**

> “I would first check latency metrics to identify where time is being spent — CPU, DB, or external calls. Then I’d compare recent changes from deployment, look for slow queries or blocking code in logs, and isolate the bottleneck layer before fixing it.”

***

## 2️⃣ DB connections are getting exhausted during peak traffic. What do you check first?

**Answer:**

> “I’d check connection pool metrics to see if connections are not being released or held too long. Then I’d inspect slow queries, transaction scope, and blocking operations inside DB calls, since connection exhaustion is usually caused by slow or stuck queries.”

***

## 3️⃣ Service works in staging but fails in production. How do you investigate?

**Answer:**

> “I’d compare configuration, traffic patterns, and data differences between staging and production. Then check logs, metrics, and dependency behavior in production, since most issues arise from environment differences, not code.”

***

## 4️⃣ Kafka consumer lag keeps increasing. What could be wrong?

**Answer:**

> “I’d first check message processing time and lag metrics to see if the consumer is slower than the producer. Then I’d inspect dependencies like DB or external APIs inside processing logic and optimize or scale the consumer.”

***

## 5️⃣ Duplicate payments are being created. How do you prevent this?

**Answer:**

> “I’d implement idempotency using a unique request ID and enforce database constraints. That ensures retries or duplicate requests don’t create multiple entries.”

***

## 6️⃣ Memory issue appears after several hours. How would you debug?

**Answer:**

> “I’d monitor heap usage over time and GC behavior. If memory is not released after GC, I’d take a heap dump to identify retained objects and trace which part of the code is holding references.”

***

## 7️⃣ Retry mechanism causes even more traffic during failures. Why?

**Answer:**

> “That’s typically a retry storm caused by no backoff or retry limits. I’d verify retry configuration and fix it using exponential backoff, limits, and circuit breakers.”

***

## 8️⃣ CPU is low but requests are timing out. What could be wrong?

**Answer:**

> “Low CPU with high latency usually means threads are waiting, not working. I’d check thread dumps, external dependencies, and blocking operations like DB or API calls.”

***

## 9️⃣ Cache improves performance but causes inconsistent data. Why?

**Answer:**

> “That usually happens due to improper cache invalidation or long TTL. I’d check when cache is updated and align it with database writes to ensure consistency.”

***

## 🔟 Thread pool exhaustion under moderate traffic. What would you inspect?

**Answer:**

> “I’d check thread pool metrics and thread dumps to see if threads are blocked or waiting on I/O. The issue is usually blocking operations or incorrect pool sizing.”

***

## 1️⃣1️⃣ Logs exist but tracing request across services is difficult. What would you do?

**Answer:**

> “I’d implement distributed tracing by adding correlation IDs across services so a request can be tracked end-to-end.”

***

## 1️⃣2️⃣ Deployment suddenly increases 4xx errors. What changed?

**Answer:**

> “I’d check request validation changes, API contract changes, or authentication issues introduced in the deployment, since 4xx usually indicates client-side request issues.”

***

## 1️⃣3️⃣ One slow DB query affects the entire system. How to identify it?

**Answer:**

> “I’d enable slow query logs or use monitoring tools to identify queries with high execution time and then optimize them using indexing or query redesign.”

***

## 1️⃣4️⃣ Scheduled jobs run multiple times in distributed instances. Why?

**Answer:**

> “This happens when multiple instances trigger the same job. I’d implement distributed locking or use a scheduler that supports single execution across instances.”

***

## 1️⃣5️⃣ APIs return stale data after horizontal scaling. Why?

**Answer:**

> “This is usually due to multiple instances having inconsistent caches. I’d use a centralized cache like Redis and ensure proper invalidation.”

***

## 1️⃣6️⃣ Application becomes slow during GC cycles. Why?

**Answer:**

> “I’d check GC logs to identify pause times and allocation rate. High object creation or large heap size often causes GC pauses.”

***

## 1️⃣7️⃣ Message processing works locally but fails intermittently in production queues. Why?

**Answer:**

> “I’d check retry logic, message acknowledgment, and consumer errors. Production issues often involve retries, duplicates, or dependency failures.”

***

## 1️⃣8️⃣ Even after auto-scaling, latency spikes under high traffic. Why?

**Answer:**

> “Scaling instances doesn’t fix bottlenecks like DB, cache, or external services. I’d identify the slow component causing contention.”

***

## 1️⃣9️⃣ Data inconsistency between two microservices. How do you debug?

**Answer:**

> “I’d trace the data flow and check if eventual consistency or async processing is involved. Then verify message delivery, retries, and synchronization between services.”

***

## 2️⃣0️⃣ Production issue cannot be reproduced locally. What’s your approach?

**Answer:**

> “I’d rely on production metrics, logs, and tracing, compare environment differences, and try to reproduce using production-like data or load patterns.”

***

# ✅ ✅ MASTER INTERVIEW CLOSING LINE

Use this when interviewer pushes deeper:

> **“I approach production issues by first identifying symptoms, narrowing down the affected layer using metrics, validating with logs and tracing, and then fixing the root cause instead of assuming.”**

