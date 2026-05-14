# Scenario: Duplicate Payments Being Created

---

## Problem

- Multiple requests create duplicate payments
- Financial inconsistency

---

## Possible Causes

- Retry mechanism without idempotency
- Network timeouts causing duplicate requests
- No uniqueness constraint
- Concurrent processing
- Message reprocessing in queues

---

## Step-by-Step Debugging Approach

### 1️⃣ Check Request Flow

- Are retries happening?
- Are requests duplicated by client?

---

### 2️⃣ Check Logs

- Same request processed multiple times?
- Multiple inserts for same transaction?

---

### 3️⃣ Check DB Constraints

- Is there a unique constraint?
- Are duplicate keys allowed?

---

### 4️⃣ Check Retry Logic

- Are retries idempotent?
- Are failures triggering repeated execution?

---

### 5️⃣ Check Messaging System (if used)

- Kafka/SQS reprocessing?
- Consumer crash causing re-delivery?

---

## Common Root Causes

✅ Missing idempotency  
✅ No unique key constraint  
✅ Retry without safeguards  
✅ At-least-once delivery systems  

---

## How to Fix

✅ Use idempotency keys  
✅ Add unique constraints  
✅ Make payment API idempotent  
✅ Handle retries safely  
✅ Deduplicate messages  

---

## Interview Answer (1–2 Lines)

> “I would ensure idempotency using unique request identifiers, enforce database constraints, and make retry mechanisms safe so the same payment is not processed twice.”

---

## Key Insight

> **Payment systems are not about success — they are about safe retries.**
