package com.gigforce.modules.feature_chat.repositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface SyncContactsRemoteService {
    @GET
    suspend fun startSyncingUploadedContactsWithGigforceUsers(
        @Url apiUrl: String ,
        @Query("uid") uid: String
    ): Response<ResponseBody>

}