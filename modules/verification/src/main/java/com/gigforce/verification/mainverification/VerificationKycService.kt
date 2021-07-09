package com.gigforce.verification.mainverification

import com.google.gson.JsonObject
import com.squareup.okhttp.RequestBody
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface VerificationKycService {

    @Multipart
    @POST
    suspend fun getKycOcrResult(
        @Url getKycOcrUrl : String,
        @Field("data") data: String,
        @Part("file") file: MultipartBody.Part
    ): Response<KycOcrResultModel>


    @POST
    suspend fun getKycVerificationService(
        @Url getKycVerifyUrl : String,
        @Body jsonObject: KycVerifyReqModel
    ): Response<KycOcrResultModel>
}