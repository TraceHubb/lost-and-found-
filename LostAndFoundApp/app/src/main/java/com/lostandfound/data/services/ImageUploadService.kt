package com.lostandfound.data.services

import android.content.Context
import android.net.Uri
import com.lostandfound.data.api.ImgurApi
import com.lostandfound.data.api.ImgurResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream

object ImageUploadService {
    // Imgur Client ID (Anonymous uploads - free tier)
    private const val IMGUR_CLIENT_ID = "546c25a59c58ad7"
    private const val IMGUR_BASE_URL = "https://api.imgur.com/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Client-ID $IMGUR_CLIENT_ID")
                .build()
            chain.proceed(request)
        }
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(IMGUR_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val imgurApi = retrofit.create(ImgurApi::class.java)

    /**
     * Upload image to Imgur and return the public URL
     */
    suspend fun uploadImage(context: Context, imageUri: Uri): Result<String> {
        return try {
            // Convert URI to File
            val file = uriToFile(context, imageUri)
            
            // Create request body
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
            
            // Upload to Imgur
            val response = imgurApi.uploadImage(imagePart)
            
            // Clean up temp file
            file.delete()
            
            if (response.isSuccessful && response.body()?.success == true) {
                val imageUrl = response.body()?.data?.link
                if (imageUrl != null) {
                    Result.success(imageUrl)
                } else {
                    Result.failure(Exception("Image URL not found in response"))
                }
            } else {
                Result.failure(Exception("Upload failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Convert URI to temporary File
     */
    private fun uriToFile(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("Cannot open input stream")
        
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        
        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        
        return tempFile
    }
}
