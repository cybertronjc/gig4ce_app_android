package com.gigforce.common_ui.remote.verification

import com.gigforce.ambassador.user_rollment.kycdocs.KycVerifyReqModel
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface VerificationKycService {

    @Multipart
    @POST
    suspend fun getKycOcrResult(
        @Url getKycOcrUrl: String,
        @Part("data") data: OCRQueryModel,
        @Part file: MultipartBody.Part
    ): Response<KycOcrResultModel>


    @POST
    suspend fun getKycVerificationService(
        @Url getKycVerifyUrl: String,
        @Body jsonObject: KycVerifyReqModel
    ): Response<KycOcrResultModel>
}