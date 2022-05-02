package com.gigforce.app.data.repositoriesImpl.tl_workspace.workspace_configuration

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection

interface TlWorkSpaceRemoteDataStore {

    suspend fun getWorkSpaceConfiguration() : List<TLWorkSpaceSection>



}