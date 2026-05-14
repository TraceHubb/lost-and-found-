package com.lostandfound.data.services

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Properties
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Service for sending emails using JavaMail API with Gmail SMTP.
 * 
 * IMPORTANT: For production use, you should:
 * 1. Create a dedicated Gmail account for the app
 * 2. Enable "App Passwords" in Google Account settings
 * 3. Store credentials securely (not hardcoded)
 */
object EmailService {
    private const val TAG = "EmailService"
    
    // TODO: Replace with your app's email credentials
    // For production, store these in BuildConfig or Firebase Remote Config
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = "587"
    private const val SENDER_EMAIL = "your-app-email@gmail.com" // Replace with your Gmail
    private const val SENDER_PASSWORD = "your-app-password" // Replace with App Password
    private const val SENDER_NAME = "Lost & Found App"
    
    /**
     * Send OTP verification email to the user.
     * 
     * @param recipientEmail The email address to send the OTP to
     * @param otpCode The 6-digit OTP code
     * @param recipientName Optional name of the recipient
     * @return true if email was sent successfully, false otherwise
     */
    suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        recipientName: String = "User"
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to send OTP email to: $recipientEmail")
            
            // Configure mail session properties
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.ssl.protocols", "TLSv1.2")
            }
            
            // Create session with authentication
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })
            
            // Create email message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, SENDER_NAME))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Your Lost & Found Verification Code"
                
                // Set HTML content
                setContent(createOtpEmailHtml(otpCode, recipientName), "text/html; charset=utf-8")
            }
            
            // Send email
            Transport.send(message)
            
            Log.d(TAG, "✅ OTP email sent successfully to: $recipientEmail")
            Log.d(TAG, "OTP Code: $otpCode") // Keep for debugging
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send OTP email to: $recipientEmail", e)
            Log.e(TAG, "Error details: ${e.message}")
            
            // Log the OTP for development/testing purposes
            Log.d(TAG, "⚠️ Email failed, but OTP code is: $otpCode")
            
            false
        }
    }
    
    /**
     * Create HTML email body for OTP verification.
     */
    private fun createOtpEmailHtml(otpCode: String, recipientName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                        max-width: 600px;
                        margin: 0 auto;
                        padding: 20px;
                        background-color: #f5f5f5;
                    }
                    .container {
                        background: linear-gradient(135deg, #8B5CF6 0%, #EC4899 100%);
                        border-radius: 16px;
                        padding: 40px;
                        text-align: center;
                    }
                    .content {
                        background: white;
                        border-radius: 12px;
                        padding: 32px;
                        margin-top: 20px;
                    }
                    .otp-code {
                        font-size: 36px;
                        font-weight: bold;
                        letter-spacing: 8px;
                        color: #8B5CF6;
                        background: #F3E8FF;
                        padding: 20px;
                        border-radius: 8px;
                        margin: 24px 0;
                    }
                    .header {
                        color: white;
                        font-size: 28px;
                        font-weight: bold;
                        margin: 0;
                    }
                    .subheader {
                        color: rgba(255, 255, 255, 0.9);
                        font-size: 16px;
                        margin-top: 8px;
                    }
                    .warning {
                        background: #FEF3C7;
                        border-left: 4px solid #F59E0B;
                        padding: 12px;
                        margin-top: 20px;
                        text-align: left;
                        border-radius: 4px;
                    }
                    .footer {
                        color: #6B7280;
                        font-size: 14px;
                        margin-top: 24px;
                        text-align: center;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1 class="header">🔐 Verification Code</h1>
                    <p class="subheader">Lost & Found App</p>
                    
                    <div class="content">
                        <p>Hi $recipientName,</p>
                        <p>You've requested to claim an item. Please use the verification code below to complete the process:</p>
                        
                        <div class="otp-code">$otpCode</div>
                        
                        <p>This code will expire in <strong>5 minutes</strong>.</p>
                        
                        <div class="warning">
                            <strong>⚠️ Security Notice:</strong><br>
                            Never share this code with anyone. Our team will never ask for your verification code.
                        </div>
                        
                        <div class="footer">
                            <p>If you didn't request this code, please ignore this email.</p>
                            <p>© 2024 Lost & Found App. All rights reserved.</p>
                        </div>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
    
    /**
     * Send a notification email when an item is claimed successfully.
     */
    suspend fun sendClaimSuccessEmail(
        recipientEmail: String,
        itemName: String,
        finderContact: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Attempting to send claim success email to: $recipientEmail")
            
            val props = Properties().apply {
                put("mail.smtp.auth", "true")
                put("mail.smtp.starttls.enable", "true")
                put("mail.smtp.host", SMTP_HOST)
                put("mail.smtp.port", SMTP_PORT)
                put("mail.smtp.ssl.protocols", "TLSv1.2")
            }
            
            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(SENDER_EMAIL, SENDER_PASSWORD)
                }
            })
            
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(SENDER_EMAIL, SENDER_NAME))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                subject = "Item Claim Successful - $itemName"
                
                setContent("""
                    <!DOCTYPE html>
                    <html>
                    <body style="font-family: Arial, sans-serif; padding: 20px; background-color: #f5f5f5;">
                        <div style="max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; padding: 32px;">
                            <h2 style="color: #10B981;">✅ Claim Successful!</h2>
                            <p>Great news! Your claim for <strong>$itemName</strong> has been verified.</p>
                            <p>You can now contact the finder using the information below:</p>
                            <div style="background: #F3E8FF; padding: 16px; border-radius: 8px; margin: 20px 0;">
                                <p style="margin: 0;"><strong>Finder's Contact:</strong></p>
                                <p style="margin: 8px 0 0 0; color: #8B5CF6; font-size: 16px;">$finderContact</p>
                            </div>
                            <hr style="border: none; border-top: 1px solid #E5E7EB; margin: 24px 0;">
                            <p style="color: #6B7280; font-size: 14px; text-align: center;">Lost & Found App</p>
                        </div>
                    </body>
                    </html>
                """.trimIndent(), "text/html; charset=utf-8")
            }
            
            Transport.send(message)
            
            Log.d(TAG, "✅ Claim success email sent to: $recipientEmail")
            true
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to send claim success email", e)
            false
        }
    }
}
