package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.data.remote.bodyFromBaseResponseElseThrow
import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceRemoteDataStore @Inject constructor(
    private val remoteService: TLWorkSpaceService
)  {

    suspend fun getTLWorkSpaceSection(
        requiredSectionIdsAndFilters: GetTLWorkspaceRequest
    ): List<TLWorkSpaceSectionApiModel> {
        return remoteService
            .getTLWorkSpaceHomeScreenData(
                requiredSectionIdsAndFilters
            ).bodyFromBaseResponseElseThrow()
    }


}