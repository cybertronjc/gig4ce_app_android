package com.gigforce.verification.mainverification

import com.google.gson.JsonObject
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Url

interface VerificationKycService {

    @POST
    suspend fun getKycOcrResult(
        @Url getKycOcrUrl : String,
        @Body jsonObject: JsonObject,
        @Part image: MultipartBody.Part
    ): Response<KYCImageRecogModel>
}