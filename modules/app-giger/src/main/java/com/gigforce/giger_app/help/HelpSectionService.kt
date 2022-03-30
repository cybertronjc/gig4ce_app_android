package com.gigforce.giger_app.help

import com.gigforce.common_ui.viewdatamodels.BaseResponse
import retrofit2.Response
import retrofit2.http.POST

interface HelpSectionService {
    @POST("profiles/getHelpQA")
    suspend fun getHelpSectionData():Response<BaseResponse<HelpSectionDM>>

}