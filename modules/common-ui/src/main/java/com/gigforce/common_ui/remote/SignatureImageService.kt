package com.gigforce.common_ui.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface SignatureImageService {

    @Multipart
    @POST("api/visit/storeimages")
    suspend fun uploadSignatureImage(
        @Part image: MultipartBody.Part
    ): Response<String>

}