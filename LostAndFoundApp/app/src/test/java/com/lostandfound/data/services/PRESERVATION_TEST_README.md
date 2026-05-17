# Preservation Property Tests - Task 2

## Overview

This document describes the preservation property tests created for Task 2 of the email delivery bugfix spec. These tests verify that existing email service functionality remains unchanged after installing the Firebase "Trigger Email" extension.

## Test File

**Location**: `app/src/test/java/com/lostandfound/data/services/EmailServicePreservationTest.kt`

## Test Properties

The preservation tests validate the following requirements:

### Property 2.1: OTP Generation Preservation (Requirement 3.1)
- **What it tests**: OTP codes are always 6 digits with numeric characters
- **Iterations**: 50 test cases with random email addresses
- **Validates**: 
  - OTP codes are exactly 6 digits long
  - OTP codes contain only numeric characters (0-9)
  - OTP codes are properly formatted with leading zeros
  - OTP values are in valid range [0, 999999]

### Property 2.2: Firestore Document Structure Preservation (Requirement 3.2)
- **What it tests**: Firestore documents have correct structure {to, message: {subject, html}}
- **Iterations**: 20 test cases with random emails and OTP codes
- **Validates**:
  - Email queuing returns success result
  - Documents have "to" field with email array
  - Documents have "message" field with "subject" and "html" subfields
  - Optional "from" field is included if sender email is configured

### Property 2.3: Logcat Logging Preservation (Requirement 3.3)
- **What it tests**: OTP codes are logged to Logcat for testing when delivery fails
- **Validates**:
  - Logging behavior exists in EmailService code
  - OTP codes are logged with "OTP for testing" message
  - Logging is preserved after fix

### Property 2.4: Rate Limiting Preservation (Requirement 3.4)
- **What it tests**: Rate limiting enforces MAX_RESEND_PER_HOUR = 3
- **Validates**:
  - Rate limiting constant is set to 3
  - Rate limiting logic exists in ClaimsRepository
  - Users cannot request more than 3 OTP resends per hour

### Property 2.5: Delivery Method Fallback Order Preservation (Requirement 3.5)
- **What it tests**: Delivery methods are tried in order: EmailJS → Firestore → SMTP
- **Validates**:
  - EmailJS is attempted first (if configured)
  - Firestore queue is attempted second
  - SMTP is attempted last (if configured)
  - Fallback order is correct

### Property 2.6: Email HTML Template Preservation (Requirements 3.1, 3.2)
- **What it tests**: Email HTML templates include recipient name, OTP code, and expiration
- **Iterations**: 20 test cases with random names and OTP codes
- **Validates**:
  - Template includes recipient name
  - Template includes OTP code
  - Template includes "Expires in 5 minutes" notice
  - Template includes proper styling (font-size, font-weight)

### Property 2.7: OTP Expiration Time Preservation (Requirement 3.1)
- **What it tests**: OTP sessions expire exactly 5 minutes after creation
- **Iterations**: 30 test cases with random creation times
- **Validates**:
  - Expiration is exactly 5 minutes (300,000 milliseconds) after creation
  - Expiration time is in the future relative to creation time
  - Expiration calculation is correct

### Property 2.8: Concurrent Email Queuing Preservation (Requirement 3.2)
- **What it tests**: Concurrent email queuing works without conflicts
- **Validates**:
  - Multiple emails can be queued simultaneously
  - No race conditions occur during queuing
  - All 10 concurrent emails are successfully queued

## Testing Framework

- **Framework**: Kotest with property-based testing
- **Dependencies**: 
  - `io.kotest:kotest-runner-junit5:5.8.0`
  - `io.kotest:kotest-assertions-core:5.8.0`
  - `io.kotest:kotest-property:5.8.0`
- **Test Style**: StringSpec (descriptive test names)

## Expected Outcome

**These tests should PASS on unfixed code** (before installing the Firebase extension).

The tests verify baseline behavior that must be preserved:
- ✅ OTP generation works correctly
- ✅ Firestore document structure is correct
- ✅ Logging behavior exists
- ✅ Rate limiting is enforced
- ✅ Fallback order is correct
- ✅ Email templates are properly formatted
- ✅ Expiration times are correct
- ✅ Concurrent queuing works

## Current Status

**Status**: Tests created but not yet executed

**Reason**: The main codebase has unrelated compilation errors in:
- `app/src/main/java/com/lostandfound/presentation/matches/MatchResultsScreen.kt`
- `app/src/main/java/com/lostandfound/presentation/navigation/AppNavGraph.kt`

These errors are related to missing claim-related imports and are unrelated to the email service functionality being tested.

**Errors**:
```
Unresolved reference: claim
Unresolved reference: ClaimViewModel
Unresolved reference: OtpInputScreen
Unresolved reference: ContactInfoScreen
```

## Next Steps

1. **Fix compilation errors** in the main codebase (unrelated to this task)
2. **Run preservation tests**: `./gradlew :app:test`
3. **Verify all tests pass** on unfixed code
4. **Proceed to Task 3**: Install Firebase "Trigger Email" extension
5. **Re-run preservation tests** after fix to ensure no regressions

## How to Run Tests (Once Compilation Errors Are Fixed)

```bash
# Run all unit tests
./gradlew :app:test

# Run only preservation tests (if supported)
./gradlew :app:test --tests "com.lostandfound.data.services.EmailServicePreservationTest"
```

## Test Design Rationale

### Why Property-Based Testing?

Property-based testing is ideal for preservation testing because:

1. **Generates many test cases**: Each property runs 10-50 iterations with random inputs
2. **Catches edge cases**: Random generation finds cases manual tests might miss
3. **Strong guarantees**: Proves properties hold across all inputs, not just specific examples
4. **Regression detection**: If behavior changes after fix, tests will fail

### Why These Specific Properties?

Each property maps directly to a preservation requirement from the design document:

- **Req 3.1**: OTP generation and expiration (Properties 2.1, 2.6, 2.7)
- **Req 3.2**: Firestore document structure (Properties 2.2, 2.8)
- **Req 3.3**: Logcat logging (Property 2.3)
- **Req 3.4**: Rate limiting (Property 2.4)
- **Req 3.5**: Delivery method fallback order (Property 2.5)

### Test Independence

Each property test is independent and can run in any order. This ensures:
- No test depends on another test's state
- Tests can be run in parallel
- Failures are isolated and easy to debug

## Observation-First Methodology

These tests follow the observation-first methodology described in the design document:

1. **Observe behavior on UNFIXED code**: Tests run on current code before installing extension
2. **Document baseline behavior**: Tests capture what currently works correctly
3. **Verify preservation after fix**: Re-run tests after installing extension
4. **Detect regressions**: If tests fail after fix, existing functionality was broken

This approach ensures the fix (installing Firebase extension) doesn't break existing email service functionality.
