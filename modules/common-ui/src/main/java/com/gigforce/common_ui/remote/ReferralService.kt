package com.gigforce.common_ui.remote

import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigResponse
import com.gigforce.common_ui.viewdatamodels.referral.ReferralRequest
import com.gigforce.core.datamodels.auth.UserAuthStatusModel
import retrofit2.Response
import retrofit2.http.*

interface ReferralService {

    @POST("message/whatsapp/send")
    suspend fun sendReferralThroughWhatsApp(
        @Body request: ReferralRequest
    ): Response<AssignGigResponse>


}