package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import kotlinx.coroutines.flow.Flow

interface TlWorkSpaceLocalDataStore {

     fun getCachedWorkspaceSectionAsFlow() : Flow<List<TLWorkSpaceSection>>

    suspend fun updateDefaultTLWorkspaceData(
        configuration : List<TLWorkSpaceSection>
    )
}