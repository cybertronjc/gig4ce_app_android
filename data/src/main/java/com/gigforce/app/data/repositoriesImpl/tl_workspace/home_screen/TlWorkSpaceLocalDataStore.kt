package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceLocalDataStore @Inject constructor() {

    fun getCachedWorkspaceSectionAsFlow(): Flow<List<TLWorkSpaceSectionApiModel>> {
        TODO("Not yet implemented")
    }

    suspend fun updateDefaultTLWorkspaceData(workSpaceData: List<TLWorkSpaceSectionApiModel>) {
        TODO("Not yet implemented")
    }


}