package com.gigforce.app.data.remote.datastoreImpls.tl_workspace

import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceRemoteDataStore
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TlWorkSpaceRemoteDataStoreImpl @Inject constructor() : TlWorkSpaceRemoteDataStore {

    override suspend fun getTLWorkSpaceSection(
        filters: TLWorkSpaceHomeScreenRepository.GetWorkSpaceSectionFilterParams
    ): List<TLWorkSpaceSection> {
        TODO("Not yet implemented")
    }
}