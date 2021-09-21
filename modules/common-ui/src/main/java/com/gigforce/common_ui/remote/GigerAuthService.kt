package com.gigforce.common_ui.remote

import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface GigerAuthService {

    @GET
    suspend fun getGigersAuthStatus(
        @Url getGigersAuthUrl : String,
        @Query("mobile") mobile: String
    ): Response<UserAuthStatusModel>


}