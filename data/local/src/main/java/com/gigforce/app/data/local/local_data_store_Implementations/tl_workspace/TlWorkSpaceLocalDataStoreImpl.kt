package com.gigforce.app.data.local.local_data_store_Implementations.tl_workspace

import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceLocalDataStore
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceLocalDataStoreImpl @Inject constructor(): TlWorkSpaceLocalDataStore {

    override fun getCachedWorkspaceSectionAsFlow(): Flow<List<TLWorkSpaceSectionApiModel>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateDefaultTLWorkspaceData(workSpaceData: List<TLWorkSpaceSectionApiModel>) {
        TODO("Not yet implemented")
    }


}