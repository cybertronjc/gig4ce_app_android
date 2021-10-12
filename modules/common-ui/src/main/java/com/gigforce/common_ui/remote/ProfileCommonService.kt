package com.gigforce.common_ui.remote

import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.*

interface ProfileCommonService {

    @GET("auth/check")
    suspend fun getUserInfoFromMobile(
        @Query("mobile") mobileNo10Digit: String
    ): Response<UserAuthStatusModel>
}