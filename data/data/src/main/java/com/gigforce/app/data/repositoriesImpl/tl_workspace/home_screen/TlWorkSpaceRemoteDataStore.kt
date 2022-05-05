package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel

interface TlWorkSpaceRemoteDataStore {

    suspend fun getTLWorkSpaceSection(
        requiredSectionIdsAndFilters : GetTLWorkspaceRequest
    ) : List<TLWorkSpaceSectionApiModel>

}