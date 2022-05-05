package com.gigforce.app.data.remote.retrofitServices

import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import retrofit2.Response
import retrofit2.http.*

interface TLWorkSpaceService {

    @POST("gigerAttendanceReport/attendanceHistoryList")
    suspend fun getTLWorkSpaceHomeScreenData(
        @Body request : GetTLWorkspaceRequest
    ): Response<List<TLWorkSpaceSectionApiModel>>
}