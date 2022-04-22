package com.gigforce.core.retrofit

import com.gigforce.core.datamodels.ambassador.*
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import com.gigforce.core.datamodels.verification.*
import retrofit2.Response
import retrofit2.http.*




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

    @GET
    suspend fun getGigersAuthStatus(
        @Url getGigersAuthUrl : String,
        @Query("mobile") mobile: String
    ): Response<UserAuthStatusModel>
}