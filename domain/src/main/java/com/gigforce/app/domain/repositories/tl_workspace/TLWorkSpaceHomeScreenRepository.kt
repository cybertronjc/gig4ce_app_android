package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.GetTLWorkspaceRequest
import com.gigforce.app.domain.models.tl_workspace.RequestedDataItem
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import com.gigforce.core.utils.Lce
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TLWorkSpaceHomeScreenRepository {

    fun getWorkspaceSectionAsFlow(): Flow<Lce<List<TLWorkSpaceSectionApiModel>>>

    suspend fun refreshCachedWorkspaceSectionData()

    suspend fun getWorkspaceSectionsData(
        requiredSectionIdsAndFilters :  GetTLWorkspaceRequest
    ): List<TLWorkSpaceSectionApiModel>

    suspend fun getSingleWorkSpaceSectionData(
        requiredSectionIdAndFilters: RequestedDataItem
    ): TLWorkSpaceSectionApiModel
}