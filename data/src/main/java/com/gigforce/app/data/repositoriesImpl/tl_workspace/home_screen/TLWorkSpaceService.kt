package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.data.remote.BaseResponse
import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import retrofit2.Response
import retrofit2.http.*

interface TLWorkSpaceService {

    @POST("operationsChampion/tlWorkspace")
    suspend fun getTLWorkSpaceHomeScreenData(
        @Body request : GetTLWorkspaceRequest
    ): Response<BaseResponse<List<TLWorkSpaceSectionApiModel>>>
}