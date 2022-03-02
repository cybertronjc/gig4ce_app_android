package com.gigforce.verification.mainverification.vaccine

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface DownloadFileService {

    @GET
    suspend fun downloadAttachment(
        @Url fullAttachmentUrl: String
    ): Response<ResponseBody>
}