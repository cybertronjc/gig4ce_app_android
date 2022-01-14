package com.gigforce.common_ui.remote.verification


import com.gigforce.common_ui.viewdatamodels.verification.SubmitSignatureRequest
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

    @POST
    suspend fun onConfirmButton(
        @Url getKycVerifyUrl: String,
        @Body jsonObject: UserConsentRequest
    ): Response<UserConsentResponse>

    @Multipart
    @POST
    suspend fun uploadVaccineCertificate(
        @Url getKycOcrUrl: String,
        @Part("data") data: VaccineFileUploadReqDM,
        @Part file: MultipartBody.Part
    ): Response<VaccineFileUploadResDM>

    @POST("profiles/updateGigerSignature")
    suspend fun uploadSignature(
        @Body submitSignatureRequest: SubmitSignatureRequest
    ): Response<KycOcrResultModel>
}