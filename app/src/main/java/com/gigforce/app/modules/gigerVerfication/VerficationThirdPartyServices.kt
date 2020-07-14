package com.gigforce.app.modules.gigerVerfication

import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface VerficationThirdPartyServices {

    @POST("sync/extract/ind_aadhaar")
    fun postPanDetails(
        @Body postData: PanCardDataModel
    ): Response<String>
}