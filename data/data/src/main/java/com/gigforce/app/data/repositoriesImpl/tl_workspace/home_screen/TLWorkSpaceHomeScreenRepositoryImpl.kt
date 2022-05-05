package com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen

import com.dropbox.android.external.store4.*
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSectionApiModel
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.core.utils.Lce
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TLWorkSpaceHomeScreenRepositoryImpl @Inject constructor(
    private val localDataStore: TlWorkSpaceLocalDataStore,
    private val remoteDataStore: TlWorkSpaceRemoteDataStore,
) : TLWorkSpaceHomeScreenRepository {

    private val store: Store<String, List<TLWorkSpaceSectionApiModel>> = StoreBuilder.from(
        fetcher = Fetcher.of { _: String ->
            getWorkspaceSectionsData()
        },
        sourceOfTruth = SourceOfTruth.Companion.of(
            reader = { key -> localDataStore.getCachedWorkspaceSectionAsFlow() },
            writer = { key: String, input: List<TLWorkSpaceSectionApiModel> ->
                localDataStore.updateDefaultTLWorkspaceData(
                    input
                )
            }
        )
    ).build()

    override fun getWorkspaceSectionAsFlow(): Flow<Lce<List<TLWorkSpaceSectionApiModel>>> {
        return store.stream(
            StoreRequest.cached(
                key = "tl_workspace_home_without_any_filter",
                refresh = true
            )
        )
            .flowOn(Dispatchers.IO)
            .map { response ->
                when (response) {
                    is StoreResponse.Loading -> {
                        print("[Store 4] Loading from ${response.origin} \n")
                        Lce.loading()
                    }
                    is StoreResponse.Error -> {
                        print("[Store 4] Error from  ${response.origin}  \n")
                        Lce.error(response.errorMessageOrNull()!!)
                    }
                    is StoreResponse.Data -> {
                        Lce.content(response.value)
                    }
                    is StoreResponse.NoNewData -> Lce.content(emptyList())
                }
            }
    }

    override suspend fun refreshCachedWorkspaceSectionData() {
        store.fresh("tl_workspace_home_without_any_filter")
    }

    override suspend fun getWorkspaceSectionsData(): List<TLWorkSpaceSectionApiModel> {
        return remoteDataStore.getTLWorkSpaceSection(
            TLWorkSpaceHomeScreenRepository.GetWorkSpaceSectionFilterParams.defaultFilters()
        )
    }

    override suspend fun getSingleWorkSpaceSectionData(
        filters: TLWorkSpaceHomeScreenRepository.GetWorkSpaceSectionFilterParams
    ): TLWorkSpaceSectionApiModel {
        return remoteDataStore.getTLWorkSpaceSection(
            filters
        ).first()
    }
}