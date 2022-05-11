package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import kotlinx.coroutines.flow.Flow

interface TlWorkSpaceLocalDataStore {

     fun getCachedWorkspaceSectionAsFlow() : Flow<List<TLWorkSpaceSectionApiModel>>

    suspend fun updateDefaultTLWorkspaceData(
        workSpaceData : List<TLWorkSpaceSectionApiModel>
    )
}