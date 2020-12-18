package com.gigforce.app.modules.chatmodule.remote

import com.gigforce.app.modules.wallet.models.PaySlipResponseModel
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface SyncContactsService {
    @GET
    suspend fun startSyncingUploadedContactsWithGigforceUsers(
        @Url apiUrl: String ,
        @Query("uid") uid: String
    ): Response<ResponseBody>

}