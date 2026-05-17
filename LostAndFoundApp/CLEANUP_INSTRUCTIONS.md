# Database Cleanup Instructions

Before implementing the new Security Questions Verification System, you need to clean up the existing database records.

## Option 1: Using Firebase Console (Recommended)

1. **Go to Firebase Console**: https://console.firebase.google.com/
2. **Select your project**: Choose your Lost & Found project
3. **Navigate to Firestore Database**: Click on "Firestore Database" in the left sidebar
4. **Delete Collections**:
   - Find the `items` collection and delete all documents
   - Find the `otpVerificationSessions` collection and delete all documents
   - Optionally delete `auditLogs` collection if you want to start fresh

## Option 2: Using the Cleanup Utility (Advanced)

I've created a `DatabaseCleanup` utility class that you can use programmatically:

### Files Created:
- `app/src/main/java/com/lostandfound/utils/DatabaseCleanup.kt` - Cleanup utility
- `app/src/main/java/com/lostandfound/presentation/admin/DatabaseCleanupScreen.kt` - UI screen

### To use the cleanup utility:

1. **Add navigation route** (temporary):
   ```kotlin
   // In AppNavGraph.kt, add this composable:
   composable("cleanup") {
       DatabaseCleanupScreen(
           onBack = { navController.popBackStack() }
       )
   }
   ```

2. **Add cleanup button** (temporary):
   ```kotlin
   // In HomeScreen.kt, add this button somewhere:
   Button(
       onClick = { navController.navigate("cleanup") },
       colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
   ) {
       Text("⚠️ Database Cleanup")
   }
   ```

3. **Run the app** and navigate to the cleanup screen
4. **Review record counts** and click "Clean Database"
5. **Remove the cleanup code** after use

## Option 3: Manual Firestore Rules (Quick)

If you have access to Firestore directly, you can run these operations:

```javascript
// Delete all items
db.collection('items').get().then(snapshot => {
    snapshot.forEach(doc => doc.ref.delete());
});

// Delete all OTP sessions
db.collection('otpVerificationSessions').get().then(snapshot => {
    snapshot.forEach(doc => doc.ref.delete());
});
```

## What Gets Cleaned Up:

- **Items Collection**: All lost and found item reports
- **OTP Sessions**: All email verification sessions
- **Audit Logs**: (Optional) All claim attempt logs

## After Cleanup:

Once the database is clean, you can proceed with implementing the new Security Questions Verification System. The new system will:

1. Use category-specific verification forms
2. Generate dynamic security questions based on item type
3. Remove all email/OTP dependencies
4. Provide a better user experience for claiming items

## Safety Notes:

⚠️ **WARNING**: This will permanently delete all existing data!
- Make sure you don't need any existing items or claims
- Consider backing up data if needed
- Test on a development database first if possible

## Next Steps:

After cleanup, we'll proceed with:
1. Creating the design document for the new system
2. Implementing the category-specific forms
3. Building the security questions generator
4. Testing the new verification flow