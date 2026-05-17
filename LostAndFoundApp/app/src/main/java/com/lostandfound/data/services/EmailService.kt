package com.lostandfound.data.services

import android.util.Log
import com.lostandfound.BuildConfig
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
 * ⚠️ SECURITY WARNING - FOR STUDENT/TESTING PROJECTS ONLY ⚠️
 * 
 * This service sends emails directly from the Android app using Gmail SMTP.
 * Gmail credentials are stored in BuildConfig (visible in decompiled APK).
 * 
 * DO NOT USE IN PRODUCTION - Use a backend server instead!
 * 
 * Sends OTP verification emails directly via Gmail SMTP (smtp.gmail.com)
 * using JavaMail with Gmail App Password authentication.
 * 
 * Configuration:
 * - Add to local.properties:
 *   SENDER_EMAIL=your-gmail@gmail.com
 *   SENDER_APP_PASSWORD=your-16-char-app-password
 */
object EmailService {
    private const val TAG = "EmailService"
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT = 587
    private const val SENDER_NAME = "Lost & Found App"

    enum class DeliveryType {
        /** Sent directly from device via SMTP */
        SENT_SMTP,
        /** Queued in Firestore for backend processing */
        QUEUED_FIRESTORE
    }

    sealed class EmailResult {
        data class Success(val delivery: DeliveryType) : EmailResult()
        data class Failure(val userMessage: String, val debugMessage: String? = null) : EmailResult()
    }

    // Credentials from BuildConfig (configured in local.properties)
    private val senderEmail: String get() = BuildConfig.SENDER_EMAIL.trim()
    private val senderAppPassword: String get() = BuildConfig.SENDER_APP_PASSWORD.trim()

    /**
     * Check if SMTP credentials are configured
     */
    fun isConfigured(): Boolean {
        val configured = senderEmail.isNotBlank() && senderAppPassword.isNotBlank()
        Log.d(TAG, "📧 Configuration check:")
        Log.d(TAG, "Sender email: ${if (senderEmail.isNotBlank()) senderEmail else "NOT SET"}")
        Log.d(TAG, "App password: ${if (senderAppPassword.isNotBlank()) "SET (${senderAppPassword.length} chars)" else "NOT SET"}")
        if (!configured) {
            Log.e(TAG, "⚠️ SMTP not configured! Add SENDER_EMAIL and SENDER_APP_PASSWORD to local.properties")
        }
        return configured
    }

    /**
     * Create authenticator for Gmail SMTP
     */
    private fun createAuthenticator() = object : Authenticator() {
        override fun getPasswordAuthentication() =
            PasswordAuthentication(senderEmail, senderAppPassword)
    }

    /**
     * Create SMTP session with STARTTLS (port 587)
     * Simplified configuration to avoid SSL handshake issues
     */
    private fun createSmtpSession(): Session {
        Log.d(TAG, "📧 Creating SMTP session (STARTTLS port $SMTP_PORT)")
        val props = Properties().apply {
            // Basic SMTP settings
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT.toString())
            
            // STARTTLS settings (simplified)
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.ssl.protocols", "TLSv1.2")
            
            // Timeouts
            put("mail.smtp.connectiontimeout", "30000")
            put("mail.smtp.timeout", "30000")
            put("mail.smtp.writetimeout", "30000")
        }
        return Session.getInstance(props, createAuthenticator())
    }

    /**
     * Send email directly via Gmail SMTP using STARTTLS
     */
    private suspend fun sendViaSmtp(
        recipientEmail: String,
        subject: String,
        htmlBody: String
    ): EmailResult = withContext(Dispatchers.IO) {
        
        // Check configuration
        if (!isConfigured()) {
            return@withContext EmailResult.Failure(
                userMessage = "Email not configured. Add credentials to local.properties",
                debugMessage = "Missing SENDER_EMAIL or SENDER_APP_PASSWORD in BuildConfig"
            )
        }

        Log.d(TAG, "========================================")
        Log.d(TAG, "📧 Preparing to send email")
        Log.d(TAG, "To: $recipientEmail")
        Log.d(TAG, "From: $senderEmail")
        Log.d(TAG, "Subject: $subject")
        Log.d(TAG, "========================================")

        var transport: Transport? = null
        return@withContext try {
            // Create session
            val session = createSmtpSession()
            
            // Create message
            val message = MimeMessage(session).apply {
                setFrom(InternetAddress(senderEmail, SENDER_NAME))
                setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
                this.subject = subject
                setContent(htmlBody, "text/html; charset=utf-8")
            }
            
            Log.d(TAG, "🔌 Connecting to SMTP server...")
            Log.d(TAG, "Host: $SMTP_HOST")
            Log.d(TAG, "Port: $SMTP_PORT")
            Log.d(TAG, "Username: $senderEmail")
            
            // Get transport and connect
            transport = session.getTransport("smtp")
            transport.connect(SMTP_HOST, SMTP_PORT, senderEmail, senderAppPassword)
            
            Log.d(TAG, "✅ SMTP connected successfully!")
            Log.d(TAG, "📤 Sending message...")
            
            // Send message
            transport.sendMessage(message, message.allRecipients)
            
            Log.d(TAG, "========================================")
            Log.d(TAG, "✅ EMAIL SENT SUCCESSFULLY!")
            Log.d(TAG, "========================================")
            
            EmailResult.Success(DeliveryType.SENT_SMTP)
            
        } catch (e: javax.mail.AuthenticationFailedException) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ AUTHENTICATION FAILED")
            Log.e(TAG, "Error: ${e.message}")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Check:")
            Log.e(TAG, "1. Gmail App Password is correct: $senderAppPassword")
            Log.e(TAG, "2. 2-Step Verification is enabled")
            Log.e(TAG, "3. App Password hasn't been revoked")
            e.printStackTrace()
            EmailResult.Failure(
                userMessage = "Authentication failed. Check Gmail App Password.",
                debugMessage = "AuthenticationFailedException: ${e.message}"
            )
        } catch (e: java.net.SocketException) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ NETWORK ERROR")
            Log.e(TAG, "Error: ${e.message}")
            Log.e(TAG, "========================================")
            Log.e(TAG, "Possible causes:")
            Log.e(TAG, "1. No internet connection")
            Log.e(TAG, "2. Mobile network blocking SMTP ports")
            Log.e(TAG, "3. Firewall blocking connection")
            Log.e(TAG, "Try: Switch to WiFi")
            e.printStackTrace()
            EmailResult.Failure(
                userMessage = "Network error. Try switching to WiFi.",
                debugMessage = "SocketException: ${e.message}"
            )
        } catch (e: Exception) {
            Log.e(TAG, "========================================")
            Log.e(TAG, "❌ SMTP SEND FAILED")
            Log.e(TAG, "Error type: ${e.javaClass.simpleName}")
            Log.e(TAG, "Error message: ${e.message}")
            Log.e(TAG, "========================================")
            e.printStackTrace()
            EmailResult.Failure(
                userMessage = "Failed to send email. Check connection and settings.",
                debugMessage = "${e.javaClass.simpleName}: ${e.message}"
            )
        } finally {
            try {
                transport?.close()
                Log.d(TAG, "🔌 SMTP transport closed")
            } catch (e: Exception) {
                Log.w(TAG, "Error closing transport: ${e.message}")
            }
        }
    }

    /**
     * Send OTP verification email
     * 
     * @param recipientEmail User's email address
     * @param otpCode 6-digit OTP code
     * @param recipientName User's name (default: "User")
     * @return EmailResult indicating success or failure
     */
    suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        recipientName: String = "User"
    ): EmailResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "========================================")
            Log.d(TAG, "📨 SENDING OTP EMAIL - START")
            Log.d(TAG, "To: $recipientEmail")
            Log.d(TAG, "OTP Code: $otpCode")
            Log.d(TAG, "Recipient Name: $recipientName")
            Log.d(TAG, "========================================")
            
            val result = sendViaSmtp(
                recipientEmail = recipientEmail,
                subject = "Your Lost & Found Verification Code",
                htmlBody = createOtpEmailHtml(otpCode, recipientName)
            )
            
            when (result) {
                is EmailResult.Success -> {
                    Log.d(TAG, "✅ OTP email sent successfully to $recipientEmail")
                    Log.d(TAG, "OTP Code (for testing): $otpCode")
                }
                is EmailResult.Failure -> {
                    Log.e(TAG, "❌ Failed to send OTP email")
                    Log.e(TAG, "User message: ${result.userMessage}")
                    Log.e(TAG, "Debug message: ${result.debugMessage}")
                    Log.w(TAG, "⚠️ OTP for testing (delivery failed): $otpCode")
                }
            }
            
            result
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error in sendOtpEmail", e)
            Log.w(TAG, "⚠️ OTP for testing: $otpCode")
            EmailResult.Failure(
                userMessage = "Failed to send verification email.",
                debugMessage = "Exception: ${e.message}"
            )
        }
    }

    /**
     * Send claim success notification email
     * 
     * @param recipientEmail User's email address
     * @param itemName Name of the claimed item
     * @param finderContact Finder's contact information
     * @return EmailResult indicating success or failure
     */
    suspend fun sendClaimSuccessEmail(
        recipientEmail: String,
        itemName: String,
        finderContact: String
    ): EmailResult = withContext(Dispatchers.IO) {
        Log.d(TAG, "📨 Sending claim success email to $recipientEmail")
        
        sendViaSmtp(
            recipientEmail = recipientEmail,
            subject = "Item Claim Successful - $itemName",
            htmlBody = createClaimSuccessEmailHtml(itemName, finderContact)
        )
    }

    /**
     * Create HTML content for OTP email
     */
    private fun createOtpEmailHtml(otpCode: String, recipientName: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">Lost & Found</h1>
                    <p style="color: #f0f0f0; margin: 10px 0 0 0;">Verification Code</p>
                </div>
                
                <div style="background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;">
                    <p style="font-size: 16px; margin-bottom: 20px;">Hi <strong>$recipientName</strong>,</p>
                    
                    <p style="font-size: 16px; margin-bottom: 20px;">
                        Your verification code for claiming your lost item:
                    </p>
                    
                    <div style="background: #f8f9fa; border: 2px dashed #667eea; border-radius: 8px; padding: 20px; text-align: center; margin: 30px 0;">
                        <p style="font-size: 36px; font-weight: bold; letter-spacing: 8px; color: #667eea; margin: 0;">
                            $otpCode
                        </p>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        ⏱️ This code will expire in <strong>5 minutes</strong>.
                    </p>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        If you didn't request this code, please ignore this email.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">
                        Lost & Found App - Helping you reunite with your belongings
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * Create HTML content for claim success email
     */
    private fun createClaimSuccessEmailHtml(itemName: String, finderContact: String): String {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; max-width: 600px; margin: 0 auto; padding: 20px;">
                <div style="background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%); padding: 30px; text-align: center; border-radius: 10px 10px 0 0;">
                    <h1 style="color: white; margin: 0; font-size: 28px;">✅ Claim Successful!</h1>
                </div>
                
                <div style="background: #ffffff; padding: 30px; border: 1px solid #e0e0e0; border-top: none; border-radius: 0 0 10px 10px;">
                    <p style="font-size: 16px; margin-bottom: 20px;">
                        Great news! Your claim for <strong>$itemName</strong> has been verified.
                    </p>
                    
                    <div style="background: #f0fdf4; border-left: 4px solid #10b981; padding: 15px; margin: 20px 0;">
                        <p style="margin: 0; font-size: 14px; color: #065f46;">
                            <strong>Finder Contact:</strong><br>
                            $finderContact
                        </p>
                    </div>
                    
                    <p style="font-size: 14px; color: #666; margin-top: 20px;">
                        Please contact the finder to arrange pickup of your item.
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #e0e0e0; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #999; text-align: center; margin: 0;">
                        Lost & Found App - Helping you reunite with your belongings
                    </p>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}