package com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface DropGigerService {

    @GET("operationsChampion/tlWorkspace/dropGigerReasons")
    suspend fun getDropReasons(): Response<BaseResponse<List<DropOptionApiModel>>>

    @POST("operationsChampion/tlWorkspace/dropGiger")
    suspend fun dropGiger(
        @Body request : DropGigerRequest
    ): Response<BaseResponse<JSONObject>>
}