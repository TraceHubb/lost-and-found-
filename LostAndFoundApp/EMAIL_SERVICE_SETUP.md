# Email Service Setup Guide

## Overview

The Lost & Found app uses an email service to send OTP (One-Time Password) verification codes when users claim items. This document explains how to set up email sending for production.

## Current Implementation

The app includes an `EmailService` class that sends verification emails with OTP codes. Currently, it's configured for development mode and logs OTP codes to the console.

## Production Setup Options

### Option 1: Firebase Cloud Functions (Recommended)

Firebase Cloud Functions is the recommended approach as it integrates seamlessly with your existing Firebase setup.

#### Steps:

1. **Install Firebase CLI**
   ```bash
   npm install -g firebase-tools
   firebase login
   ```

2. **Initialize Cloud Functions**
   ```bash
   firebase init functions
   ```

3. **Install Email Service (SendGrid or Nodemailer)**
   ```bash
   cd functions
   npm install @sendgrid/mail
   # OR
   npm install nodemailer
   ```

4. **Create Email Function** (`functions/index.js`):
   ```javascript
   const functions = require('firebase-functions');
   const sgMail = require('@sendgrid/mail');
   
   sgMail.setApiKey(functions.config().sendgrid.key);
   
   exports.sendOtpEmail = functions.https.onCall(async (data, context) => {
     // Verify the request is authenticated
     if (!context.auth) {
       throw new functions.https.HttpsError('unauthenticated', 'User must be authenticated');
     }
     
     const { recipientEmail, otpCode, recipientName } = data;
     
     const msg = {
       to: recipientEmail,
       from: 'noreply@yourdomain.com',
       subject: 'Your Lost & Found Verification Code',
       html: `
         <h2>Verification Code</h2>
         <p>Hi ${recipientName},</p>
         <p>Your verification code is: <strong>${otpCode}</strong></p>
         <p>This code will expire in 5 minutes.</p>
       `
     };
     
     try {
       await sgMail.send(msg);
       return { success: true };
     } catch (error) {
       console.error('Error sending email:', error);
       throw new functions.https.HttpsError('internal', 'Failed to send email');
     }
   });
   ```

5. **Set SendGrid API Key**
   ```bash
   firebase functions:config:set sendgrid.key="YOUR_SENDGRID_API_KEY"
   ```

6. **Deploy Function**
   ```bash
   firebase deploy --only functions
   ```

7. **Update Android App**
   
   In `EmailService.kt`, update the `EMAIL_API_ENDPOINT` to your Cloud Function URL:
   ```kotlin
   private const val EMAIL_API_ENDPOINT = "https://us-central1-your-project.cloudfunctions.net/sendOtpEmail"
   ```

### Option 2: SendGrid Direct API

If you prefer to call SendGrid directly from the Android app:

1. **Get SendGrid API Key**
   - Sign up at https://sendgrid.com
   - Create an API key with "Mail Send" permissions

2. **Update EmailService.kt**
   ```kotlin
   private const val EMAIL_API_ENDPOINT = "https://api.sendgrid.com/v3/mail/send"
   private const val API_KEY = BuildConfig.SENDGRID_API_KEY // Store in BuildConfig
   ```

3. **Add API Key to gradle.properties** (local only, don't commit):
   ```properties
   SENDGRID_API_KEY=your_api_key_here
   ```

4. **Update app/build.gradle.kts**:
   ```kotlin
   android {
       defaultConfig {
           buildConfigField("String", "SENDGRID_API_KEY", "\"${project.findProperty("SENDGRID_API_KEY")}\"")
       }
   }
   ```

### Option 3: Mailgun API

Similar to SendGrid, but using Mailgun:

1. Sign up at https://www.mailgun.com
2. Get your API key and domain
3. Update `EMAIL_API_ENDPOINT` to Mailgun's API
4. Adjust the JSON payload format to match Mailgun's API

## Email Templates

The app includes two email templates:

### 1. OTP Verification Email
- Sent when a user initiates a claim
- Contains a 6-digit verification code
- Expires in 5 minutes
- Includes security warnings

### 2. Claim Success Email
- Sent after successful verification
- Confirms the claim was successful
- Provides finder's contact information

## Security Considerations

1. **Never commit API keys** to version control
2. **Use environment variables** or BuildConfig for sensitive data
3. **Implement rate limiting** on the backend to prevent abuse
4. **Validate email addresses** before sending
5. **Use HTTPS** for all API calls
6. **Implement proper authentication** for Cloud Functions

## Testing

### Development Mode
Currently, the app is in development mode:
- OTP codes are logged to Android Logcat
- Email sending always returns success
- No actual emails are sent

To test with real emails:
1. Set up one of the production options above
2. Update `EmailService.kt` to return actual API responses
3. Change the return values from `true` to `isSuccessful`

### Production Mode
Before deploying to production:
1. Remove development logging of OTP codes
2. Enable actual email sending
3. Test with real email addresses
4. Monitor email delivery rates
5. Set up email bounce handling

## Monitoring

### Recommended Monitoring:
- Email delivery success rate
- Failed email attempts
- OTP expiration rates
- User verification success rates

### Firebase Analytics Events:
```kotlin
// Log email sent
analytics.logEvent("otp_email_sent") {
    param("user_id", userId)
    param("success", success)
}

// Log verification success
analytics.logEvent("otp_verified") {
    param("user_id", userId)
    param("attempts", attemptCount)
}
```

## Cost Estimates

### SendGrid
- Free tier: 100 emails/day
- Essentials: $19.95/month for 50,000 emails

### Mailgun
- Free tier: 5,000 emails/month
- Foundation: $35/month for 50,000 emails

### Firebase Cloud Functions
- Free tier: 2M invocations/month
- Paid: $0.40 per million invocations

## Troubleshooting

### Emails not being received
1. Check spam/junk folders
2. Verify sender email is authenticated (SPF, DKIM)
3. Check email service logs for errors
4. Verify API key is correct

### OTP codes expiring too quickly
- Adjust `OTP_EXPIRY_MINUTES` in `ClaimsRepository.kt`
- Default is 5 minutes

### Rate limiting issues
- Implement exponential backoff
- Add user-facing rate limit messages
- Monitor `MAX_RESEND_PER_HOUR` setting

## Support

For issues with:
- **SendGrid**: https://support.sendgrid.com
- **Mailgun**: https://help.mailgun.com
- **Firebase**: https://firebase.google.com/support

## Next Steps

1. Choose an email service provider
2. Set up API credentials
3. Update `EmailService.kt` with production settings
4. Test thoroughly in staging environment
5. Deploy to production
6. Monitor email delivery metrics
