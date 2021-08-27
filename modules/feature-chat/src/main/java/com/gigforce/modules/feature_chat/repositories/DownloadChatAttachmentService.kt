package com.gigforce.modules.feature_chat.repositories

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url


interface DownloadChatAttachmentService {

    @GET
    suspend fun downloadAttachment(
        @Url fullAttachmentUrl: String
    ): Response<ResponseBody>


    @GET("https://qslv7bpakk.execute-api.ap-south-1.amazonaws.com/default/sync-user-uploaded-contacts_with_gig_force_users_prod")
    suspend fun trySyncingContacts(
        @Query("uid") uid: String
    ): Response<ResponseBody>
}