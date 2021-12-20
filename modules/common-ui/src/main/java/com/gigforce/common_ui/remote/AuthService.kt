package com.gigforce.common_ui.remote

import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AuthService {

    @GET("auth/createUser")
    suspend fun getOrCreateUserInAuthAndProfile(
        @Query("mobileNumber") mobileNumber: String,
        @Query("userName") userName: String
    ): Response<UserAuthStatusModel>
}