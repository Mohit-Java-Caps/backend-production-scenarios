
<!--
TAGS: memory-leak, java, jvm, gc, backend-debugging, production-issues, performance
-->

# Scenario: Memory Issue After Several Hours (Possible Memory Leak)

---

## Problem

- Application runs fine initially
- Memory usage keeps increasing
- Eventually causes crashes or GC pressure

---

## Possible Causes

- Memory leak (objects not released)
- Cache growing indefinitely
- Static collections holding references
- Improper object lifecycle
- GC misconfiguration

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Memory Metrics

- Heap usage over time
- GC frequency and pause time

Look for:
> Continuous growth (not decreasing after GC)

---

### 2️⃣ Analyze GC Behavior

- Frequent full GC?
- Long pauses?

---

### 3️⃣ Capture Heap Dump

- Identify large object groups
- Look for retained objects

---

### 4️⃣ Identify Object Retention

Check:
- Collections growing over time
- Caches not evicting data
- Static references

---

### 5️⃣ Check Application Code Changes

- New caching logic?
- New collections?
- Long-living objects introduced?

---

## Common Root Causes

✅ Unbounded cache  
✅ Static variables holding objects  
✅ Collection growth  
✅ ThreadLocal misuse  
✅ Unreleased resources  

---

## How to Fix

✅ Add cache limits/eviction  
✅ Remove unnecessary references  
✅ Use proper object lifecycle  
✅ Tune GC if needed  

---

## Interview Answer (1–2 Lines)

> “I would analyze memory metrics and GC behavior, capture a heap dump to identify retained objects, and trace back to the code holding references and fix the leak.”

---

## Key Insight

> **Memory leaks are not about allocation — they are about retention.**
``
