package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.ambassador_user_enrollment.models.*
import com.gigforce.app.modules.verification.models.IdfyResponse
import com.gigforce.app.modules.verification.models.PostDataDL
import com.gigforce.app.modules.verification.models.PostDataOCRs
import com.gigforce.app.modules.verification.models.PostDataPAN
import io.reactivex.Observable
import retrofit2.Response
import retrofit2.http.*

interface IdfyApiAadhaar {
    @POST("sync/extract/ind_aadhaar")
    @Headers(
        "Content-Type:text/plain",
        "Content-Type: application/json;charset=UTF-8",
        "account-id:fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4",
        "api-key:1bc58043-00fb-4799-bea3-93a012d174bb"
    )
    fun postOCR(
        @Body postData: PostDataOCRs
    ): Observable<IdfyResponse> // body data
}

interface IdfyApiDL {
    @POST("sync/extract/ind_driving_license")
    @Headers(
        "Content-Type:text/plain",
        "Content-Type: application/json;charset=UTF-8",
        "account-id:fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4",
        "api-key:1bc58043-00fb-4799-bea3-93a012d174bb"
    )
    fun postDL(
        @Body postData: PostDataDL
    ): Observable<IdfyResponse> // body data
}

interface IdfyApiPAN {
    @POST("sync/extract/ind_pan")
    @Headers(
        "Content-Type:text/plain",
        "Content-Type: application/json;charset=UTF-8",
        "account-id:fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4",
        "api-key:1bc58043-00fb-4799-bea3-93a012d174bb"
    )
    fun postPAN(
        @Body postData: PostDataPAN
    ): Observable<IdfyResponse> // body data
}


interface CreateUserAccEnrollmentAPi {
    @POST
    suspend fun createUser(
        @Url fullUrl: String,
        @Body postData: List<CreateUserRequest>
    ): Response<List<CreateUserResponse>> // body data

    @POST
    suspend fun registerMobile(
        @Url fullUrl: String,
        @Body postData: RegisterMobileNoRequest
    ): Response<RegisterMobileNoResponse> // body data

    @GET
    suspend fun verifyOtp(
        @Url fullUrl: String,
        @Query("token") token: String,
        @Query("otp") otp: String
    ): Response<VerifyOtpResponse> // body data

    @GET
    suspend fun loadCityAndStateUsingPincode(
        @Url fullUrl: String
    ): Response<List<PincodeResponse>> // body data
}