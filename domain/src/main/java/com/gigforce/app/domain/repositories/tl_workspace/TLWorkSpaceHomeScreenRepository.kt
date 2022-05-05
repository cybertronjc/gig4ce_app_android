package com.gigforce.app.domain.repositories.tl_workspace

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import com.gigforce.core.utils.Lce
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface TLWorkSpaceHomeScreenRepository {

    fun getWorkspaceSectionAsFlow(): Flow<Lce<List<TLWorkSpaceSectionApiModel>>>

    suspend fun refreshCachedWorkspaceSectionData()

    suspend fun getWorkspaceSectionsData(): List<TLWorkSpaceSectionApiModel>

    suspend fun getSingleWorkSpaceSectionData(
        filters: GetWorkSpaceSectionFilterParams
    ): TLWorkSpaceSectionApiModel


    data class GetWorkSpaceSectionFilterParams(
        val type: String,
        val startDate: LocalDateTime,
        val endDate: LocalDateTime,
    ) {

        companion object {

            fun defaultFilters(): GetWorkSpaceSectionFilterParams {
                return GetWorkSpaceSectionFilterParams(
                    type = "",
                    startDate = LocalDateTime.now(),
                    endDate = LocalDateTime.now()
                )
            }
        }
    }
}