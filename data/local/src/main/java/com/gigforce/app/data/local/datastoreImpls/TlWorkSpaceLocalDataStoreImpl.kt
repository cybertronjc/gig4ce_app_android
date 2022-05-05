package com.gigforce.app.data.local.datastoreImpls

import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceLocalDataStore
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceLocalDataStoreImpl @Inject constructor(): TlWorkSpaceLocalDataStore {

    override fun getCachedWorkspaceSectionAsFlow(): Flow<List<TLWorkSpaceSection>> {
        TODO("Not yet implemented")
    }

    override suspend fun updateDefaultTLWorkspaceData(configuration: List<TLWorkSpaceSection>) {
        TODO("Not yet implemented")
    }
}