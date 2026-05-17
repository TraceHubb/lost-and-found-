# Bug Condition Exploration Test - Email Delivery

## Overview

This document explains the bug condition exploration test for the email delivery bug in the Lost & Found app.

## Bug Description

**Current Behavior (Defect):**
- Users initiate a claim and the system attempts to send an OTP email
- Email is successfully queued in Firestore `mail` collection
- Firebase "Trigger Email" extension is NOT installed
- Email is NEVER delivered to recipient's inbox
- Users cannot complete claim verification

**Root Cause:**
The Firebase "Trigger Email" extension is not installed. This extension monitors the Firestore `mail` collection and sends emails via Gmail SMTP on Firebase servers. Without it, queued emails remain unprocessed indefinitely.

## Test Approach

### Property-Based Testing

The test uses **Kotest property-based testing** to generate multiple test cases automatically. This provides stronger guarantees than example-based testing.

**Test File:** `EmailDeliveryBugConditionTest.kt`

### Test Properties

**Property 1: Bug Condition - Email Never Delivered When Extension Not Installed**

```
For any email delivery attempt where:
  - Email is successfully queued in Firestore `mail` collection
  - Firebase "Trigger Email" extension is not installed/configured

The bug manifests as:
  - Email is never delivered to recipient's inbox
  - Firestore document remains in `mail` collection indefinitely
  - User cannot complete OTP verification
```

### Test Structure

1. **ARRANGE**: Set up test scenario with random email addresses and OTP codes
2. **ACT**: Call `EmailService.sendOtpEmail()` to queue email in Firestore
3. **ASSERT**: 
   - Verify email was queued successfully (returns `QUEUED_FIRESTORE`)
   - Wait up to 60 seconds for email delivery
   - Check if Firestore document is deleted (indicates email was sent)
   - **EXPECT FAILURE**: Email never delivered, document never deleted

## Expected Outcomes

### On UNFIXED Code (Before Installing Extension)

**EXPECTED OUTCOME: TEST FAILS** ✓

This is the CORRECT outcome! The test failure proves the bug exists:

```
Test: "Property 1: Email delivery fails when Firebase Trigger Email extension is not installed"
Status: FAILED
Reason: emailDelivered shouldBe true
  Expected: true
  Actual: false

Counterexample found:
  recipientEmail: "test@example.com"
  otpCode: "123456"
  emailQueued: true
  emailDelivered: false (after 60 seconds)
  firestoreDocumentDeleted: false
```

**What This Proves:**
- ✓ Email queuing logic works correctly
- ✓ Firestore document is created with proper structure
- ✗ Email is never delivered (BUG CONFIRMED)
- ✗ Document remains in `mail` collection indefinitely

### After Installing Firebase Extension

**EXPECTED OUTCOME: TEST PASSES** ✓

Once the Firebase "Trigger Email" extension is installed and configured:

```
Test: "Property 1: Email delivery fails when Firebase Trigger Email extension is not installed"
Status: PASSED

All test cases passed:
  - Email queued successfully
  - Email delivered within 60 seconds
  - Firestore document deleted after processing
  - OTP code received in inbox
```

**What This Proves:**
- ✓ Extension is installed and active
- ✓ Extension processes queued emails
- ✓ Emails are delivered to recipients
- ✓ Bug is FIXED

## Running the Test

### Prerequisites

1. **Firebase Project**: Ensure Firebase is configured in `google-services.json`
2. **Internet Connection**: Required for Firestore access
3. **Test Dependencies**: Kotest, kotlinx-coroutines-test

### Run Command

```bash
./gradlew test --tests "com.lostandfound.data.services.EmailDeliveryBugConditionTest"
```

### Expected Output (Unfixed Code)

```
EmailDeliveryBugConditionTest > Property 1: Email delivery fails when Firebase Trigger Email extension is not installed FAILED
    io.kotest.assertions.AssertionFailedError: expected:<true> but was:<false>
    at EmailDeliveryBugConditionTest$1$1.invokeSuspend(EmailDeliveryBugConditionTest.kt:89)

EmailDeliveryBugConditionTest > Bug Condition: Specific case - OTP email queued but never delivered without extension FAILED
    io.kotest.assertions.AssertionFailedError: expected:<true> but was:<false>
    at EmailDeliveryBugConditionTest$2.invokeSuspend(EmailDeliveryBugConditionTest.kt:125)

2 tests completed, 2 failed
```

## Counterexamples Documented

### Counterexample 1: Generic Email Delivery Failure

```kotlin
Input:
  recipientEmail: "user@example.com"
  otpCode: "789012"
  recipientName: "Test User"

Observed Behavior:
  1. EmailService.sendOtpEmail() called
  2. Result: EmailResult.Success(DeliveryType.QUEUED_FIRESTORE)
  3. Firestore document created in `mail` collection
  4. Waited 60 seconds
  5. Document still exists (not deleted)
  6. Email never received in inbox

Bug Confirmed: Email queued but never delivered
```

### Counterexample 2: Specific Test Case

```kotlin
Input:
  recipientEmail: "test@example.com"
  otpCode: "123456"
  recipientName: "Test User"

Observed Behavior:
  1. Email queued successfully
  2. Firestore document created
  3. After 60 seconds: document still exists
  4. Email never delivered

Bug Confirmed: Extension not processing queued emails
```

## Fix Implementation

### Step 1: Install Firebase Extension

1. Open Firebase Console: https://console.firebase.google.com/project/lost-and-found-app-4cf9d
2. Navigate to Build → Extensions → Browse extensions
3. Search for "Trigger Email" (official Firebase extension)
4. Click "Install" and accept permissions

### Step 2: Configure SMTP Settings

1. SMTP host: `smtp.gmail.com`
2. SMTP port: `587` (STARTTLS) or `465` (SSL)
3. SMTP username: Gmail address
4. SMTP password: 16-character Gmail App Password
5. From address: Same Gmail address
6. Collection name: `mail` (must match `FIRESTORE_MAIL_COLLECTION`)

### Step 3: Verify Fix

Run the test again:

```bash
./gradlew test --tests "com.lostandfound.data.services.EmailDeliveryBugConditionTest"
```

**Expected Output (After Fix):**

```
EmailDeliveryBugConditionTest > Property 1: Email delivery fails when Firebase Trigger Email extension is not installed PASSED
EmailDeliveryBugConditionTest > Bug Condition: Specific case - OTP email queued but never delivered without extension PASSED

2 tests completed, 2 passed
```

## Test Maintenance

### When to Update This Test

- If email delivery timeout changes (currently 60 seconds)
- If Firestore collection name changes (currently `mail`)
- If email queuing logic changes in `EmailService.kt`
- If Firebase extension configuration changes

### Related Tests

- **Preservation Tests**: Verify existing functionality unchanged (Task 2)
- **Integration Tests**: End-to-end email delivery flow
- **Unit Tests**: EmailService individual methods

## Notes

- This test requires real Firebase connection (not mocked)
- Test may take up to 60 seconds to complete (timeout period)
- Test failure on unfixed code is EXPECTED and CORRECT
- Test documents the bug condition for future reference
