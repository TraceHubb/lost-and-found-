# Email Setup Instructions - IMPORTANT

## Quick Setup (5 minutes)

The app now uses JavaMail to send real emails through Gmail SMTP. Follow these steps to enable email sending:

### Step 1: Create a Gmail Account for the App

1. Go to https://accounts.google.com
2. Create a new Gmail account (e.g., `lostandfoundapp2024@gmail.com`)
3. Complete the setup

### Step 2: Enable App Passwords

1. Go to your Google Account settings: https://myaccount.google.com
2. Click on "Security" in the left sidebar
3. Enable "2-Step Verification" if not already enabled
4. After enabling 2-Step Verification, go back to Security
5. Click on "App passwords" (you'll see this option after enabling 2-Step Verification)
6. Select "Mail" and "Other (Custom name)"
7. Enter "Lost & Found App" as the name
8. Click "Generate"
9. **COPY THE 16-CHARACTER PASSWORD** (it will look like: `abcd efgh ijkl mnop`)

### Step 3: Update the App Code

Open `app/src/main/java/com/lostandfound/data/services/EmailService.kt` and update these lines:

```kotlin
private const val SENDER_EMAIL = "your-app-email@gmail.com" // Replace with your Gmail
private const val SENDER_PASSWORD = "your-app-password" // Replace with App Password
```

**Example:**
```kotlin
private const val SENDER_EMAIL = "lostandfoundapp2024@gmail.com"
private const val SENDER_PASSWORD = "abcd efgh ijkl mnop" // The 16-char password from Step 2
```

### Step 4: Rebuild the App

```bash
./gradlew clean assembleDebug
```

### Step 5: Test

1. Run the app
2. Try to claim an item
3. Check the email inbox of the registered user
4. You should receive the OTP code!

## Security Notes

⚠️ **IMPORTANT**: The current implementation has the email credentials hardcoded. This is OK for testing but NOT recommended for production.

### For Production:

1. **Use BuildConfig** to store credentials:
   ```kotlin
   // In app/build.gradle.kts
   android {
       defaultConfig {
           buildConfigField("String", "SENDER_EMAIL", "\"${project.findProperty("SENDER_EMAIL")}\"")
           buildConfigField("String", "SENDER_PASSWORD", "\"${project.findProperty("SENDER_PASSWORD")}\"")
       }
   }
   
   // In EmailService.kt
   private const val SENDER_EMAIL = BuildConfig.SENDER_EMAIL
   private const val SENDER_PASSWORD = BuildConfig.SENDER_PASSWORD
   ```

2. **Store in local.properties** (not committed to git):
   ```properties
   SENDER_EMAIL=your-email@gmail.com
   SENDER_PASSWORD=your-app-password
   ```

3. **Or use Firebase Remote Config** for even better security

## Troubleshooting

### Email not sending?

1. **Check Logcat** for error messages:
   ```
   adb logcat | grep EmailService
   ```

2. **Common issues:**
   - ❌ Wrong email/password → Check credentials
   - ❌ "Less secure app access" error → Make sure you're using App Password, not regular password
   - ❌ Network error → Check internet connection
   - ❌ "Authentication failed" → Regenerate App Password

### Still not working?

The app will log the OTP code to Logcat even if email fails:
```
⚠️ Email failed, but OTP code is: 123456
```

You can use this code for testing while debugging email issues.

## Alternative: Use a Different Email Service

If you don't want to use Gmail, you can modify `EmailService.kt` to use:

### SendGrid (Recommended for production)
- Free tier: 100 emails/day
- More reliable than Gmail SMTP
- See `EMAIL_SERVICE_SETUP.md` for instructions

### Mailgun
- Free tier: 5,000 emails/month
- Good for high volume

### AWS SES
- Very cheap ($0.10 per 1,000 emails)
- Requires AWS account

## Testing Checklist

- [ ] Created Gmail account for app
- [ ] Enabled 2-Step Verification
- [ ] Generated App Password
- [ ] Updated SENDER_EMAIL in EmailService.kt
- [ ] Updated SENDER_PASSWORD in EmailService.kt
- [ ] Rebuilt the app
- [ ] Tested claim flow
- [ ] Received OTP email
- [ ] Verified OTP works
- [ ] Tested resend button
- [ ] Received success email after verification

## Current Status

✅ JavaMail library added
✅ Email service implemented
✅ HTML email templates ready
✅ OTP generation working
✅ Resend functionality working
⏳ **Waiting for email credentials to be configured**

Once you complete Steps 1-3 above, emails will be sent to real email addresses!
