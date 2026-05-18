<!--
TAGS: caching, consistency, redis, backend-debugging, distributed-systems, production-issues
-->

# Scenario: Cache Improves Performance but Causes Inconsistent Data

---

## Problem

- System becomes faster after caching
- But users receive stale or incorrect data

---

## Possible Causes

- Cache not updated on write
- TTL too long
- Multiple instances with inconsistent cache
- Write-through/write-back issues
- Cache invalidation missing

---

## Step-by-Step Debugging Approach

### 1️⃣ Identify Data Flow

- Where is cache used?
- When is data updated?

---

### 2️⃣ Check Cache Strategy

- Read-through?
- Write-through?
- Lazy loading?

---

### 3️⃣ Verify Cache Invalidation

- Is cache cleared after update?
- Is TTL appropriate?

---

### 4️⃣ Check Multi-Instance Issues

- Different instances having different cache states?

---

## Common Root Causes

✅ Missing cache invalidation  
✅ Long TTL  
✅ Multiple nodes inconsistency  
✅ Update not syncing with cache  

---

## How to Fix

✅ Use proper cache invalidation  
✅ Reduce TTL  
✅ Use distributed cache (Redis)  
✅ Implement write-through or write-around strategy  

---

## Interview Answer

> “I would first trace how and when cache is updated, check TTL and invalidation strategy, and ensure consistency by aligning cache updates with database writes.”

---

## Key Insight

> **Cache is easy to add, but hard to keep correct.**
