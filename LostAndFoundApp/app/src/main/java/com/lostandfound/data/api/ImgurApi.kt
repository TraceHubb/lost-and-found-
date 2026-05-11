package com.lostandfound.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImgurApi {
    @Multipart
    @POST("3/image")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): Response<ImgurResponse>
}

data class ImgurResponse(
    val data: ImgurData?,
    val success: Boolean,
    val status: Int
)

data class ImgurData(
    val id: String,
    val link: String,
    val deletehash: String?
)
