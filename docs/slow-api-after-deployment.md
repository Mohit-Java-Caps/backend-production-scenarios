# Scenario: API Suddenly Becomes Slow After Deployment

---

## Problem

After a deployment:
- API response time increased from 200ms → 8 seconds

---

## Possible Causes

- N+1 database queries
- New blocking calls introduced
- DB connection pool exhaustion
- Slow external API
- Thread pool exhaustion
- Cache not working

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Metrics

Look at:
- API latency
- DB query time
- CPU usage
- Thread pool usage

Identify:
> Where time is being spent

---

### 2️⃣ Compare Before vs After Deployment

Ask:
- What code changed?
- Any new DB queries?
- Any synchronous API calls added?

---

### 3️⃣ Check Logs

Look for:
- Slow queries
- Timeouts
- retries

---

### 4️⃣ Check Database

- Query performance
- Connection pool usage
- Locking issues

---

### 5️⃣ Check Thread Pool

- Active threads
- Queue size
- Blocking operations

---

## Common Root Causes

✅ Lazy loading → N+1 queries  
✅ New external API call (slow)  
✅ Missing index  
✅ Blocking I/O  
✅ Connection pool exhausted  

---

## How to Fix

✅ Optimize queries  
✅ Add indexes  
✅ Add caching  
✅ Remove blocking logic  
✅ Tune thread pool  

---

## Interview Answer (1–2 Lines)

> “I would first check metrics to identify whether the delay is in CPU, DB, or external calls, then compare changes introduced in deployment and use logs and tracing to isolate the bottleneck and fix the root cause.”

---

## Key Insight

> **Most performance issues come from recent changes.**
