package com.gigforce.app.data.repositoriesImpl.tl_workspace.workspace_configuration

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import kotlinx.coroutines.flow.Flow

interface TlWorkSpaceLocalDataStore {

     fun getCachedWorkSpaceConfiguration() : Flow<List<TLWorkSpaceSection>>

    suspend fun updateWorkSpaceConfiguration(
        configuration : List<TLWorkSpaceSection>
    )
}