package com.gigforce.app.data.repositoriesImpl.tl_workspace.workspace_configuration

import com.dropbox.android.external.store4.*
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceSection
import com.gigforce.app.domain.repositories.tl_workspace.TlWorkspaceSectionArrangementConfigurationRepository

class TlWorkspaceSectionArrangementConfigurationRepositoryImpl constructor(
    private val localDataStore: TlWorkSpaceLocalDataStore,
    private val remoteDataStore: TlWorkSpaceRemoteDataStore
) : TlWorkspaceSectionArrangementConfigurationRepository {

    private val store: Store<String, List<TLWorkSpaceSection>> = StoreBuilder.from(
        fetcher = Fetcher.of { _: String ->
            remoteDataStore.getWorkSpaceConfiguration()
        },
        sourceOfTruth = SourceOfTruth.Companion.of(
            reader = { key -> localDataStore.getCachedWorkSpaceConfiguration() },
            writer = { key: String, input: List<TLWorkSpaceSection> ->
                localDataStore.updateWorkSpaceConfiguration(
                    input
                )
            }
        )
    ).build()

    override suspend fun getSectionsListAndArrangementInfo(): List<TLWorkSpaceSection> {
     return store.get("s")
    }
}