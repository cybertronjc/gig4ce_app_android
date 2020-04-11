package com.gigforce.app.modules.verification.service

import com.gigforce.app.modules.verification.models.*
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import io.reactivex.Observable

interface IdfyApi {

    @POST("sync/extract/ind_aadhaar")
    @Headers("Content-Type:text/plain",
            "Content-Type: application/json;charset=UTF-8",
            "account-id:fd5931df2bde/f8451777-05d8-4e0f-b859-ad5dfa895bd4",
            "api-key:1bc58043-00fb-4799-bea3-93a012d174bb")
    fun postOCR(
            //@Query("Authorization") authorizationKey: String, // authentication header
            @Body postData: PostDataOCR): Observable<IdfyResponse> // body data
}