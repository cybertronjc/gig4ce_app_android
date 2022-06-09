package com.gigforce.app.data.repositoriesImpl.tl_workspace.change_client_id

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import org.json.JSONObject
import retrofit2.Response
import retrofit2.http.*

interface ChangeClientIdService {

    @POST("business/clientidsmapping")
    suspend fun changeClientId(
        @Body changeClientIdRequest: ChangeClientIdRequest
    ): Response<BaseResponse<Any>>
}