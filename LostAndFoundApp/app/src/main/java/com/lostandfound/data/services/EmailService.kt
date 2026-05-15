package com.lostandfound.data.services

import android.util.Log
import com.lostandfound.BuildConfig
import com.lostandfound.data.firebase.FirebaseProviders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Properties
import java.util.concurrent.TimeUnit
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

/**
 * Sends OTP / claim emails without SMTP from the phone (mobile networks often block ports 465/587).
 *
 * **Default (recommended):** writes to Firestore collection `mail` → Firebase extension
 * **"Trigger Email"** sends via Gmail on Google's servers.
 * See `CONFIGURE_EMAIL.txt` for one-time extension setup.
 *
 * **Optional:** EmailJS over HTTPS — set EMAILJS_* in `local.properties`.
 */
object EmailService {
    private const val TAG = "EmailService"
    private const val SMTP_HOST = "smtp.gmail.com"
    private const val SMTP_PORT_SSL = 465
    private const val SMTP_PORT_STARTTLS = 587
    private const val SENDER_NAME = "Lost & Found App"
    private const val FIRESTORE_MAIL_COLLECTION = "mail"
    private const val EMAILJS_API_URL = "https://api.emailjs.com/api/v1.0/email/send"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(25, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .writeTimeout(25, TimeUnit.SECONDS)
        .build()

    enum class DeliveryType {
        /** Sent immediately via EmailJS HTTPS API */
        SENT_EMAILJS,
        /** Sent from device SMTP */
        SENT_SMTP,
        /** Saved to Firestore `mail` — requires Firebase Trigger Email extension to deliver */
        QUEUED_FIRESTORE
    }

    sealed class EmailResult {
        data class Success(val delivery: DeliveryType) : EmailResult()
        data class Failure(val userMessage: String, val debugMessage: String? = null) : EmailResult()
    }

    private val senderEmail: String get() = BuildConfig.SENDER_EMAIL.trim()
    private val senderAppPassword: String get() = BuildConfig.SENDER_APP_PASSWORD.trim()
    private val emailJsServiceId: String get() = BuildConfig.EMAILJS_SERVICE_ID.trim()
    private val emailJsTemplateId: String get() = BuildConfig.EMAILJS_TEMPLATE_ID.trim()
    private val emailJsPublicKey: String get() = BuildConfig.EMAILJS_PUBLIC_KEY.trim()

    fun isConfigured(): Boolean = true

    private fun isEmailJsConfigured(): Boolean =
        emailJsServiceId.isNotBlank() &&
            emailJsTemplateId.isNotBlank() &&
            emailJsPublicKey.isNotBlank()

    private fun isSmtpConfigured(): Boolean =
        senderEmail.isNotBlank() && senderAppPassword.isNotBlank()

    private fun configurationError(): EmailResult.Failure =
        EmailResult.Failure(
            userMessage = "Email is not set up. Install the Firebase Trigger Email extension (see CONFIGURE_EMAIL.txt).",
            debugMessage = "No email delivery method available"
        )

    /** Firebase extension "Trigger Email" — SMTP runs on Firebase servers, not on the phone. */
    private suspend fun sendViaFirestoreQueue(
        recipientEmail: String,
        subject: String,
        htmlBody: String
    ): EmailResult = withContext(Dispatchers.IO) {
        try {
            val payload = hashMapOf<String, Any>(
                "to" to listOf(recipientEmail),
                "message" to hashMapOf(
                    "subject" to subject,
                    "html" to htmlBody
                )
            )
            if (senderEmail.isNotBlank()) {
                payload["from"] = senderEmail
            }
            FirebaseProviders.firestore
                .collection(FIRESTORE_MAIL_COLLECTION)
                .add(payload)
                .await()
            Log.d(TAG, "Queued email in Firestore/$FIRESTORE_MAIL_COLLECTION for $recipientEmail")
            Log.d(TAG, "Install Firebase 'Trigger Email' extension to deliver queued mail")
            EmailResult.Success(DeliveryType.QUEUED_FIRESTORE)
        } catch (e: Exception) {
            Log.e(TAG, "Firestore mail queue failed", e)
            EmailResult.Failure(
                userMessage = "Could not queue verification email. Check internet and Firestore rules.",
                debugMessage = e.message
            )
        }
    }

    /** HTTPS — works on mobile when SMTP ports are blocked. */
    private suspend fun sendViaEmailJs(
        recipientEmail: String,
        subject: String,
        otpCode: String?,
        recipientName: String,
        extraHtml: String? = null
    ): EmailResult = withContext(Dispatchers.IO) {
        try {
            val params = JSONObject().apply {
                put("to_email", recipientEmail)
                put("user_name", recipientName)
                put("subject", subject)
                otpCode?.let { put("otp_code", it) }
                extraHtml?.let { put("message_html", it) }
            }
            val body = JSONObject().apply {
                put("service_id", emailJsServiceId)
                put("template_id", emailJsTemplateId)
                put("user_id", emailJsPublicKey)
                put("template_params", params)
            }
            val request = Request.Builder()
                .url(EMAILJS_API_URL)
                .post(body.toString().toRequestBody("application/json".toMediaType()))
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    Log.d(TAG, "EmailJS sent to $recipientEmail")
                    EmailResult.Success(DeliveryType.SENT_EMAILJS)
                } else {
                    val err = "HTTP ${response.code}: ${response.body?.string()}"
                    Log.e(TAG, "EmailJS failed: $err")
                    EmailResult.Failure(
                        userMessage = "Failed to send verification email. Check EmailJS settings.",
                        debugMessage = err
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "EmailJS error", e)
            EmailResult.Failure(
                userMessage = "Failed to send verification email.",
                debugMessage = e.message
            )
        }
    }

    private fun createAuthenticator() = object : Authenticator() {
        override fun getPasswordAuthentication() =
            PasswordAuthentication(senderEmail, senderAppPassword)
    }

    private fun createSslSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT_SSL.toString())
            put("mail.smtp.ssl.enable", "true")
            put("mail.smtp.ssl.trust", SMTP_HOST)
            put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3")
            put("mail.smtp.socketFactory.port", SMTP_PORT_SSL.toString())
            put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory")
            put("mail.smtp.socketFactory.fallback", "false")
            put("mail.smtp.connectiontimeout", "25000")
            put("mail.smtp.timeout", "25000")
        }
        return Session.getInstance(props, createAuthenticator())
    }

    private fun createStartTlsSession(): Session {
        val props = Properties().apply {
            put("mail.smtp.auth", "true")
            put("mail.smtp.host", SMTP_HOST)
            put("mail.smtp.port", SMTP_PORT_STARTTLS.toString())
            put("mail.smtp.starttls.enable", "true")
            put("mail.smtp.starttls.required", "true")
            put("mail.smtp.ssl.trust", SMTP_HOST)
            put("mail.smtp.connectiontimeout", "25000")
            put("mail.smtp.timeout", "25000")
        }
        return Session.getInstance(props, createAuthenticator())
    }

    private fun trySmtpSend(session: Session, message: MimeMessage, port: Int, label: String): EmailResult {
        var transport: Transport? = null
        return try {
            transport = session.getTransport("smtp")
            transport.connect(SMTP_HOST, port, senderEmail, senderAppPassword)
            transport.sendMessage(message, message.allRecipients)
            Log.d(TAG, "SMTP ($label) sent")
            EmailResult.Success(DeliveryType.SENT_SMTP)
        } catch (e: Exception) {
            Log.w(TAG, "SMTP $label failed: ${e.message}")
            EmailResult.Failure(debugMessage = "$label: ${e.message}", userMessage = "")
        } finally {
            try {
                transport?.close()
            } catch (_: Exception) {
            }
        }
    }

    /** Last resort — often blocked on mobile networks. */
    private suspend fun sendViaSmtp(
        recipientEmail: String,
        subject: String,
        htmlBody: String
    ): EmailResult = withContext(Dispatchers.IO) {
        if (!isSmtpConfigured()) {
            return@withContext EmailResult.Failure(
                userMessage = "SMTP not configured.",
                debugMessage = "Missing SENDER_EMAIL / SENDER_APP_PASSWORD"
            )
        }
        val sslSession = createSslSession()
        val sslMsg = MimeMessage(sslSession).apply {
            setFrom(InternetAddress(senderEmail, SENDER_NAME))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            this.subject = subject
            setContent(htmlBody, "text/html; charset=utf-8")
        }
        if (trySmtpSend(sslSession, sslMsg, SMTP_PORT_SSL, "SSL") is EmailResult.Success) {
            return@withContext EmailResult.Success(DeliveryType.SENT_SMTP)
        }
        val tlsSession = createStartTlsSession()
        val tlsMsg = MimeMessage(tlsSession).apply {
            setFrom(InternetAddress(senderEmail, SENDER_NAME))
            setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail))
            this.subject = subject
            setContent(htmlBody, "text/html; charset=utf-8")
        }
        val tls = trySmtpSend(tlsSession, tlsMsg, SMTP_PORT_STARTTLS, "STARTTLS")
        if (tls is EmailResult.Success) return@withContext EmailResult.Success(DeliveryType.SENT_SMTP)
        EmailResult.Failure(
            userMessage = "Could not send email from this device (SMTP blocked). Install Firebase Trigger Email extension — see CONFIGURE_EMAIL.txt.",
            debugMessage = "SMTP failed on ports 465 and 587"
        )
    }

    private suspend fun deliverEmail(
        recipientEmail: String,
        subject: String,
        htmlBody: String,
        otpCode: String?,
        recipientName: String
    ): EmailResult {
        if (isEmailJsConfigured()) {
            val js = sendViaEmailJs(recipientEmail, subject, otpCode, recipientName, htmlBody)
            if (js is EmailResult.Success) return js
            Log.w(TAG, "EmailJS failed, trying Firestore queue")
        }

        val queued = sendViaFirestoreQueue(recipientEmail, subject, htmlBody)
        if (queued is EmailResult.Success) return queued

        if (isSmtpConfigured()) {
            Log.w(TAG, "Firestore queue failed, trying SMTP (may fail on mobile)")
            return sendViaSmtp(recipientEmail, subject, htmlBody)
        }

        return configurationError()
    }

    suspend fun sendOtpEmail(
        recipientEmail: String,
        otpCode: String,
        recipientName: String = "User"
    ): EmailResult = withContext(Dispatchers.IO) {
        try {
            Log.d(TAG, "Sending OTP to $recipientEmail")
            when (
                val result = deliverEmail(
                    recipientEmail = recipientEmail,
                    subject = "Your Lost & Found Verification Code",
                    htmlBody = createOtpEmailHtml(otpCode, recipientName),
                    otpCode = otpCode,
                    recipientName = recipientName
                )
            ) {
                is EmailResult.Success -> {
                    Log.d(TAG, "OTP delivery initiated for $recipientEmail")
                    result
                }
                is EmailResult.Failure -> {
                    Log.w(TAG, "OTP for testing (delivery failed): $otpCode")
                    result
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "sendOtpEmail error", e)
            Log.w(TAG, "OTP for testing: $otpCode")
            EmailResult.Failure(
                userMessage = "Failed to send verification email.",
                debugMessage = e.message
            )
        }
    }

    suspend fun sendClaimSuccessEmail(
        recipientEmail: String,
        itemName: String,
        finderContact: String
    ): EmailResult = withContext(Dispatchers.IO) {
        deliverEmail(
            recipientEmail = recipientEmail,
            subject = "Item Claim Successful - $itemName",
            htmlBody = """
                <p>Your claim for <strong>$itemName</strong> was verified.</p>
                <p>Contact the finder: <strong>$finderContact</strong></p>
            """.trimIndent(),
            otpCode = null,
            recipientName = "User"
        )
    }

    private fun createOtpEmailHtml(otpCode: String, recipientName: String): String {
        return """
            <p>Hi $recipientName,</p>
            <p>Your verification code:</p>
            <p style="font-size:28px;font-weight:bold;letter-spacing:4px;">$otpCode</p>
            <p>Expires in 5 minutes.</p>
        """.trimIndent()
    }
}
