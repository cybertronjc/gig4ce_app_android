package com.gigforce.app.data.remote.datastoreImpls.tl_workspace

import com.gigforce.app.data.remote.retrofitServices.TLWorkSpaceService
import com.gigforce.app.data.remote.utils_ktx.bodyFromBaseResponseElseThrow
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceRemoteDataStore
import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceRemoteDataStoreImpl @Inject constructor(
    private val remoteService: TLWorkSpaceService
) : TlWorkSpaceRemoteDataStore {

    override suspend fun getTLWorkSpaceSection(
        requiredSectionIdsAndFilters: GetTLWorkspaceRequest
    ): List<TLWorkSpaceSectionApiModel> {
        return remoteService
            .getTLWorkSpaceHomeScreenData(
                requiredSectionIdsAndFilters
            ).bodyFromBaseResponseElseThrow()
    }


}