<!--
TAGS: jvm, garbage-collection, performance, java, backend-debugging, production-issues
-->

# Scenario: Application Becomes Slow During Garbage Collection

---

## Problem

- Application is slow only during certain periods
- Latency spikes
- High GC pause times observed

---

## Possible Causes

- Large heap size
- High object allocation rate
- Memory leak
- Inefficient GC configuration
- Long-lived objects accumulation

---

## Step-by-Step Debugging Approach

### 1️⃣ Check GC Metrics

- GC pause duration
- Frequency of GC
- Allocation rate

---

### 2️⃣ Analyze GC Logs

Look for:
- Frequent Full GC
- Long pause times

---

### 3️⃣ Check Heap Usage Pattern

- Is heap constantly full?
- Are objects surviving multiple GC cycles?

---

### 4️⃣ Identify Allocation Hotspots

Check:
- Object creation patterns
- High memory churn areas

---

## Common Root Causes

✅ Excessive object creation  
✅ Large heap causing long pause  
✅ Memory leak  
✅ Inefficient GC tuning  

---

## How to Fix

✅ Optimize object allocation  
✅ Tune GC settings  
✅ Reduce heap size (if too large)  
✅ Fix memory leaks  

---

## Interview Answer

> “I would analyze GC metrics and logs to identify pause times and allocation patterns, then optimize memory usage or tune the GC to reduce latency spikes.”

---

## Key Insight

> **GC problems are usually allocation problems in disguise.**
