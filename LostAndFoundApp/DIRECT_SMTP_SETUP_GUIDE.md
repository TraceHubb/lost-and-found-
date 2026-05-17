# Direct Gmail SMTP Email Setup Guide

## ⚠️ SECURITY WARNING

**This implementation is for STUDENT/TESTING projects ONLY!**

Your Gmail credentials are embedded in the APK and can be extracted by anyone who decompresses your app. **DO NOT use this in production!**

For production apps, use a backend server to send emails.

---

## ✅ Setup Complete!

Your app is now configured to send OTP emails directly via Gmail SMTP using JavaMail.

### Current Configuration

**Email Service**: `app/src/main/java/com/lostandfound/data/services/EmailService.kt`
- ✅ Direct SMTP sending (no Firebase extension needed)
- ✅ Gmail SMTP with App Password authentication
- ✅ SSL (port 465) and STARTTLS (port 587) support
- ✅ Background threading with Kotlin coroutines
- ✅ Comprehensive error handling and logging
- ✅ Beautiful HTML email templates

**Credentials**: `local.properties`
```properties
SENDER_EMAIL=dagmawitadeferes@gmail.com
SENDER_APP_PASSWORD=mhxamvlnfzewqraa
```

**Dependencies**: `app/build.gradle.kts`
```kotlin
// JavaMail for sending emails
implementation("com.sun.mail:android-mail:1.6.7")
implementation("com.sun.mail:android-activation:1.6.7")
```

---

## 📧 How It Works

### 1. OTP Email Flow

```kotlin
// In your repository/viewmodel:
val result = EmailService.sendOtpEmail(
    recipientEmail = "user@example.com",
    otpCode = "123456",
    recipientName = "John Doe"
)

when (result) {
    is EmailService.EmailResult.Success -> {
        // Email sent successfully!
        Log.d(TAG, "Email sent via ${result.delivery}")
    }
    is EmailService.EmailResult.Failure -> {
        // Email failed
        Log.e(TAG, result.userMessage)
    }
}
```

### 2. SMTP Connection Process

1. **SSL Attempt (Port 465)** - Most reliable for Gmail
   - Creates secure SSL connection
   - Authenticates with Gmail App Password
   - Sends email

2. **STARTTLS Fallback (Port 587)** - If SSL fails
   - Creates TLS connection
   - Authenticates with Gmail App Password
   - Sends email

3. **Error Handling** - If both fail
   - Returns detailed error message
   - Logs OTP code to Logcat for testing
   - User sees friendly error message

### 3. Email Templates

**OTP Email**:
- Beautiful gradient header
- Large, bold OTP code display
- 5-minute expiration notice
- Professional HTML styling

**Claim Success Email**:
- Success confirmation
- Finder contact information
- Professional HTML styling

---

## 🔧 Configuration

### Gmail App Password Setup

1. **Enable 2-Step Verification**
   - Go to https://myaccount.google.com/security
   - Enable 2-Step Verification

2. **Generate App Password**
   - Go to https://myaccount.google.com/apppasswords
   - Select "Mail" and "Other (Custom name)"
   - Enter "Lost & Found App"
   - Copy the 16-character password (format: `xxxx xxxx xxxx xxxx`)

3. **Add to local.properties**
   ```properties
   SENDER_EMAIL=your-gmail@gmail.com
   SENDER_APP_PASSWORD=your16charpassword
   ```

### Build Configuration

The credentials are automatically injected into `BuildConfig` via `app/build.gradle.kts`:

```kotlin
defaultConfig {
    val senderEmail = localProperty("SENDER_EMAIL")
    val senderAppPassword = localProperty("SENDER_APP_PASSWORD").replace(" ", "")
    buildConfigField("String", "SENDER_EMAIL", "\"$senderEmail\"")
    buildConfigField("String", "SENDER_APP_PASSWORD", "\"$senderAppPassword\"")
}
```

---

## 🧪 Testing

### 1. Check Configuration

```kotlin
if (EmailService.isConfigured()) {
    Log.d(TAG, "✅ Email service is configured")
} else {
    Log.e(TAG, "❌ Email service not configured")
}
```

### 2. Send Test Email

```kotlin
viewModelScope.launch {
    val result = EmailService.sendOtpEmail(
        recipientEmail = "test@example.com",
        otpCode = "123456",
        recipientName = "Test User"
    )
    
    when (result) {
        is EmailService.EmailResult.Success -> {
            println("✅ Email sent successfully!")
        }
        is EmailService.EmailResult.Failure -> {
            println("❌ Failed: ${result.userMessage}")
            println("Debug: ${result.debugMessage}")
        }
    }
}
```

### 3. Monitor Logcat

Look for these log messages:

**Success**:
```
D/EmailService: ========================================
D/EmailService: 📨 Sending OTP email
D/EmailService: To: user@example.com
D/EmailService: OTP Code: 123456
D/EmailService: ========================================
D/EmailService: 📧 Preparing to send email to: user@example.com
D/EmailService: From: dagmawitadeferes@gmail.com
D/EmailService: Subject: Your Lost & Found Verification Code
D/EmailService: Creating SSL session for SMTP (port 465)
D/EmailService: Attempting SMTP send via SSL (port 465)...
D/EmailService: SMTP connected, sending message...
D/EmailService: ✅ Email sent successfully via SMTP (SSL)
D/EmailService: SMTP transport closed
D/EmailService: ✅ OTP email sent successfully to user@example.com
D/EmailService: OTP Code (for testing): 123456
```

**Failure**:
```
E/EmailService: ❌ SMTP SSL failed: AuthenticationFailedException: ...
E/EmailService: ❌ Failed to send OTP email
E/EmailService: User message: Could not send email...
E/EmailService: Debug message: SMTP failed on both port 465 and port 587
W/EmailService: ⚠️ OTP for testing (delivery failed): 123456
```

---

## 🐛 Troubleshooting

### Email Not Sending

**1. Check Credentials**
```bash
# Verify local.properties has correct values
cat local.properties | grep SENDER
```

**2. Check Internet Permission**
```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

**3. Check Gmail Settings**
- Ensure 2-Step Verification is enabled
- Ensure App Password is correct (16 characters, no spaces)
- Try regenerating the App Password

**4. Check Logcat**
```bash
adb logcat | grep EmailService
```

### Common Errors

**AuthenticationFailedException**
- Wrong Gmail App Password
- 2-Step Verification not enabled
- App Password revoked

**ConnectException / SocketTimeoutException**
- No internet connection
- Firewall blocking ports 465/587
- Mobile network blocking SMTP ports

**MessagingException**
- Invalid email address format
- Gmail account locked/suspended
- Too many emails sent (rate limit)

---

## 📱 Permissions

### Required Permissions

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
```

### Optional Permissions

```xml
<!-- For checking network state -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

## 🔒 Security Considerations

### Current Implementation (Student Project)

✅ **Pros**:
- Simple setup
- No backend required
- Works offline (once configured)
- Free (no Firebase Blaze plan needed)

❌ **Cons**:
- Credentials visible in decompiled APK
- Anyone can extract and abuse your Gmail
- Gmail account could be banned for spam
- Rate limits apply to your personal Gmail
- Not scalable for production

### Production Alternative

For production apps, use this architecture:

```
Android App → Backend API → Gmail SMTP
```

**Backend Options**:
1. **Firebase Cloud Functions** (Node.js)
2. **AWS Lambda** (Python/Node.js)
3. **Heroku/Railway** (Any language)
4. **Your own server** (Any language)

**Benefits**:
- Credentials stored securely on server
- Rate limiting and abuse prevention
- Scalable to thousands of users
- Can switch email providers easily
- Audit logging and monitoring

---

## 📊 Email Delivery Status

### Success Indicators

✅ Email sent successfully
✅ Logcat shows "✅ Email sent successfully via SMTP"
✅ User receives email within 30-60 seconds
✅ OTP code matches Logcat output

### Failure Indicators

❌ Logcat shows "❌ SMTP failed"
❌ User doesn't receive email after 2 minutes
❌ Error message displayed in app
❌ OTP logged with "⚠️ OTP for testing (delivery failed)"

---

## 🎨 Email Templates

### OTP Email Preview

```
┌─────────────────────────────────────┐
│   Lost & Found                      │
│   Verification Code                 │
├─────────────────────────────────────┤
│ Hi John Doe,                        │
│                                     │
│ Your verification code for claiming │
│ your lost item:                     │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │         123456                  │ │
│ └─────────────────────────────────┘ │
│                                     │
│ ⏱️ This code will expire in 5 min  │
│                                     │
│ If you didn't request this code,   │
│ please ignore this email.           │
└─────────────────────────────────────┘
```

### Claim Success Email Preview

```
┌─────────────────────────────────────┐
│   ✅ Claim Successful!              │
├─────────────────────────────────────┤
│ Great news! Your claim for          │
│ Black Backpack has been verified.   │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │ Finder Contact:                 │ │
│ │ john@example.com                │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Please contact the finder to        │
│ arrange pickup of your item.        │
└─────────────────────────────────────┘
```

---

## 📝 Code Examples

### Send OTP Email

```kotlin
// In ClaimsRepository or ViewModel
suspend fun sendOtpToUser(email: String, otp: String): Boolean {
    return withContext(Dispatchers.IO) {
        val result = EmailService.sendOtpEmail(
            recipientEmail = email,
            otpCode = otp,
            recipientName = "User"
        )
        
        result is EmailService.EmailResult.Success
    }
}
```

### Send Claim Success Email

```kotlin
// After OTP verification succeeds
suspend fun notifyClaimSuccess(
    email: String,
    itemName: String,
    finderEmail: String
) {
    EmailService.sendClaimSuccessEmail(
        recipientEmail = email,
        itemName = itemName,
        finderContact = finderEmail
    )
}
```

### Handle Email Result

```kotlin
when (val result = EmailService.sendOtpEmail(email, otp)) {
    is EmailService.EmailResult.Success -> {
        // Show success message
        _uiState.value = UiState.OtpSent(
            message = "Verification code sent to $email"
        )
    }
    is EmailService.EmailResult.Failure -> {
        // Show error message
        _uiState.value = UiState.Error(
            message = result.userMessage
        )
        // Log debug info
        Log.e(TAG, "Email failed: ${result.debugMessage}")
    }
}
```

---

## ✅ What Changed

### Removed

❌ Firebase Trigger Email extension dependency
❌ Firestore `mail` collection queuing
❌ EmailJS HTTPS API calls
❌ Complex fallback logic

### Added

✅ Direct Gmail SMTP sending
✅ SSL (port 465) support
✅ STARTTLS (port 587) fallback
✅ Comprehensive logging
✅ Beautiful HTML email templates
✅ Better error messages

### Kept

✅ Existing OTP verification flow
✅ Firestore OTP session storage
✅ Rate limiting logic
✅ Background threading with coroutines
✅ JavaMail dependencies

---

## 🚀 Next Steps

1. **Test Email Sending**
   - Run your app
   - Trigger OTP email
   - Check Logcat for success/failure
   - Verify email arrives in inbox

2. **Monitor Performance**
   - Check email delivery time
   - Monitor success/failure rates
   - Watch for Gmail rate limits

3. **Plan for Production**
   - Set up backend server
   - Move credentials to server
   - Implement proper email service
   - Add monitoring and alerts

---

## 📞 Support

If you encounter issues:

1. Check Logcat for detailed error messages
2. Verify Gmail App Password is correct
3. Ensure internet connection is active
4. Try regenerating Gmail App Password
5. Check Gmail account isn't locked

---

## 📚 References

- [JavaMail API Documentation](https://javaee.github.io/javamail/)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)
- [Android Mail Library](https://github.com/javaee/javamail/tree/master/mail)

---

**Last Updated**: May 16, 2026
**Version**: 1.0.0
**Status**: ✅ Ready for Testing
