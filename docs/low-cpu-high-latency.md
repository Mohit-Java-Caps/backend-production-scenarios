<!--
TAGS: performance, latency, backend-debugging, system-design, bottleneck-analysis, production-issues
-->

# Scenario: CPU Usage Is Low but Requests Are Timing Out

---

## Problem

- CPU usage is low
- But API requests are very slow or timing out

---

## Possible Causes

- Blocking I/O operations
- Thread pool exhaustion
- External service latency
- DB connection waits
- Lock contention

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Thread Pool Metrics

- Active threads
- Queue size

---

### 2️⃣ Analyze Thread Dumps

Look for:
- Threads waiting
- BLOCKED or WAITING states

---

### 3️⃣ Check External Dependencies

- DB response time
- External API latency

---

### 4️⃣ Check Lock Contention

- Are threads waiting for locks?
- Long synchronized blocks?

---

## Common Root Causes

✅ Threads waiting on I/O  
✅ Blocking external calls  
✅ DB connection delays  
✅ Lock contention  
✅ Thread pool saturation  

---

## How to Fix

✅ Reduce blocking operations  
✅ Use async processing  
✅ Optimize DB queries  
✅ Reduce lock contention  

---

## Interview Answer

> “Low CPU but high latency usually indicates threads are waiting, not working. I would check thread states, external dependencies, and blocking operations to identify the bottleneck.”

---

## Key Insight

> **Low CPU does not mean the system is healthy. It often means it's waiting.**
``
