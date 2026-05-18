<!--
TAGS: retries, distributed-systems, backend-debugging, resilience, production-issues, system-design
-->

# Scenario: Retry Mechanism Causes Traffic Explosion (Retry Storm)

---

## Problem

- System starts failing
- Retry logic triggers more requests
- Traffic increases exponentially
- System collapses further

---

## Possible Causes

- Immediate retries without delay
- No backoff strategy
- No retry limit
- Downstream system already failing
- Cascading failures across services

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Metrics

- Incoming request rate
- Retry count
- Error rate

Look for:
> Sudden spike in traffic AFTER failures

---

### 2️⃣ Check Logs

Look for patterns:
- Same request being retried many times
- Rapid retry loops

---

### 3️⃣ Check Retry Configuration

- Retry attempts
- Backoff strategy
- Timeout settings

---

### 4️⃣ Identify Downstream Failure

Ask:
> What service is failing first?

Retry storms usually start due to:
- DB failure
- External API failure

---

## Common Root Causes

✅ No exponential backoff  
✅ Unlimited retries  
✅ Immediate retry logic  
✅ Dependency failure triggering retries  

---

## How to Fix

✅ Add exponential backoff  
✅ Add retry limits  
✅ Use circuit breaker pattern  
✅ Fail fast instead of retrying blindly  

---

## Interview Answer

> “I would check retry patterns and request rates, identify if retries are amplifying failures, and fix it using exponential backoff, retry limits, and circuit breakers.”

---

## Key Insight

> **Retries should reduce load, not multiply it.**
