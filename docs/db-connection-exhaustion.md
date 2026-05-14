
<!--
TAGS: database, connection-pool, backend-debugging, performance, production-issues, spring-boot
-->
# Scenario: Database Connections Getting Exhausted

---

## Problem

During peak traffic:
- API starts timing out
- DB connection pool reaches max limit

---

## Possible Causes

- Connection leak (not closed properly)
- Long-running queries
- Too many concurrent requests
- Thread pool blocking DB connections
- Incorrect pool size configuration

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Metrics

- Active DB connections
- Max pool size
- Request throughput
- Query latency

---

### 2️⃣ Check Connection Pool Usage

Look for:
- Connections not being released
- Connections stuck for long time

---

### 3️⃣ Check Logs

- Slow queries
- Timeouts
- Exceptions related to DB

---

### 4️⃣ Analyze Queries

- Missing indexes?
- Full table scans?
- Locks or deadlocks?

---

### 5️⃣ Check Thread Behavior

- Are threads blocking while holding connections?
- Are external calls made inside transactions?

---

## Common Root Causes

✅ Connection leak  
✅ Slow queries under load  
✅ Large transactions  
✅ Blocking inside DB operations  
✅ Pool size too small  

---

## How to Fix

✅ Ensure connections are closed properly  
✅ Optimize queries and indexes  
✅ Reduce transaction scope  
✅ Increase connection pool size (carefully)  
✅ Avoid blocking calls inside DB transactions  

---

## Interview Answer (1–2 Lines)

> “I would check connection pool metrics first, then identify if connections are being held too long due to slow queries or blocking operations, and fix the root cause instead of just increasing pool size.”

---

## Key Insight

> **Connection exhaustion is usually a symptom, not the root problem.**
