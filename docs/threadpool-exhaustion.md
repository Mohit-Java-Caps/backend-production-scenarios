<!--
TAGS: threadpool, concurrency, java, performance, backend-debugging, production-issues
-->

# Scenario: Thread Pool Exhaustion Under Moderate Traffic

---

## Problem

- Requests start timing out
- Thread pool is fully occupied
- Queue size keeps increasing

---

## Possible Causes

- Blocking I/O inside threads
- External service latency
- Deadlocks
- Too many concurrent requests
- Incorrect thread pool size

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Thread Pool Metrics

- Active thread count
- Queue size
- Rejected requests

---

### 2️⃣ Analyze Thread Dumps

Look for:
- Threads waiting/blocking
- Deadlocks
- Long-running tasks

---

### 3️⃣ Check Task Type

Are threads:
- CPU-bound?
- Waiting on DB?
- Waiting on external API?

---

### 4️⃣ Check Blocking Calls

- `Future.get()`
- Synchronous API calls
- DB queries inside threads

---

## Common Root Causes

✅ Blocking operations inside thread pool  
✅ External dependency latency  
✅ Incorrect thread pool sizing  
✅ Long-running tasks  

---

## How to Fix

✅ Use async/non-blocking operations  
✅ Increase thread pool size (carefully)  
✅ Separate pools for different tasks  
✅ Reduce blocking calls  

---

## Interview Answer

> “I would check thread pool metrics and thread dumps to identify whether threads are blocked or slow, then remove blocking operations or resize and separate thread pools based on workload.”

---

## Key Insight

> **Thread pools don’t fail suddenly — they get saturated slowly.**
