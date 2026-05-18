<!--
TAGS: kafka, distributed-systems, event-driven, lag, backend-debugging, production-issues
-->

# Scenario: Kafka Consumer Lag Is Increasing Continuously

---

## Problem

- Kafka consumer is falling behind
- Message lag keeps increasing
- Processing delays grow over time

---

## Possible Causes

- Consumer processing too slow
- High message production rate
- Consumer crashes or restarts
- Partition imbalance
- External service delays inside consumer
- Thread pool exhaustion

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Metrics

- Consumer lag
- Message throughput
- Processing time per message

---

### 2️⃣ Check Consumer Health

- Is it running?
- Any restarts?
- Exceptions in logs?

---

### 3️⃣ Analyze Processing Time

Ask:
> How long does each message take?

Check for:
- DB calls
- External API calls
- Blocking operations

---

### 4️⃣ Check Partition Distribution

- Are partitions balanced?
- Is one consumer overloaded?

---

### 5️⃣ Check Downstream Dependencies

- Slow DB?
- Slow external APIs?
- Retry loops?

---

## Common Root Causes

✅ Slow message processing  
✅ Blocking calls inside consumer  
✅ Too few partitions  
✅ Imbalanced partitions  
✅ Dependency latency  

---

## How to Fix

✅ Increase consumer instances  
✅ Optimize processing logic  
✅ Reduce blocking operations  
✅ Add parallel processing  
✅ Tune partition count  

---

## Interview Answer

> “I would first check lag metrics and processing time, then identify whether the bottleneck is inside the consumer or due to downstream dependencies like DB or external APIs, and scale or optimize accordingly.”

---

## Key Insight

> **Lag is not the problem — slow consumption is.**
``
