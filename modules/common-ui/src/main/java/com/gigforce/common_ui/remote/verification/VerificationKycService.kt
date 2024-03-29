package com.gigforce.common_ui.remote.verification


import com.gigforce.common_ui.viewdatamodels.BaseResponse
import com.gigforce.common_ui.viewdatamodels.verification.SubmitSignatureRequest
import com.google.firebase.Timestamp
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*
import java.util.*

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
        @Part("data") data: VaccineIdLabelReqDM,
        @Part file: MultipartBody.Part
    ): Response<VaccineFileUploadResDM>

    @POST("profiles/updateGigerSignature")
    suspend fun uploadSignature(
        @Body submitSignatureRequest: SubmitSignatureRequest
    ): Response<KycOcrResultModel>

    @POST
    suspend fun confirmVaccinationData(
        @Url getKycOcrUrl: String,
        @Body data: Data1
    ): Response<BaseResponse<Any>>

    @GET
    suspend fun getComplianceData(
        @Url getComplianceDataUrl: String
    ): Response<ComplianceDataModel>

    @GET
    suspend fun getCharacterCertificateData(
        @Url getCharacterDataUrl: String
    ): Response<CharacterCertificateResponse>

    @Multipart
    @POST
    suspend fun uploadCharacterCertificate(
        @Url getUploadCharacterUrl: String,
        @Part("updatedBy") updatedBy: RequestBody,
        @Part("updatedAt") updatedAt: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<VaccineFileUploadResDM>
}