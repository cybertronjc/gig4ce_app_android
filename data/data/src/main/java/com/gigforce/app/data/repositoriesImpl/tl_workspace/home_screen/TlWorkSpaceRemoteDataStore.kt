package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository

interface TlWorkSpaceRemoteDataStore {

    suspend fun getTLWorkSpaceSection(
        filters: TLWorkSpaceHomeScreenRepository.GetWorkSpaceSectionFilterParams
    ) : List<TLWorkSpaceSection>

}