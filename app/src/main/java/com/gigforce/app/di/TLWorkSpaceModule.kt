package com.gigforce.app.di

import com.gigforce.app.data.local.local_data_store_Implementations.tl_workspace.TlWorkSpaceLocalDataStoreImpl
import com.gigforce.app.data.remote.datastoreImpls.tl_workspace.TlWorkSpaceRemoteDataStoreImpl
import com.gigforce.app.data.remote.datastoreImpls.tl_workspace.TlWorkspaceUpcomingGigersRemoteDatastoreImpl
import com.gigforce.app.data.remote.retrofitServices.TLWorkSpaceService
import com.gigforce.app.data.remote.retrofitServices.TLWorkUpcomingGigersService
import com.gigforce.app.data.remote.retrofit_services.TlWorkspaceUpcomingGigersRemoteDatastore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending.TLWorkspaceComplianceRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TLWorkSpaceHomeScreenRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceLocalDataStore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceRemoteDataStore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.retention.TLWorkspaceRetentionRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.TLWorkspaceUpcomingGigersRepositoryImpl
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceComplianceRepository
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceRetentionRepository
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkspaceUpcomingGigersRepository
import com.gigforce.core.retrofit.RetrofitServiceFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

@Module
@InstallIn(SingletonComponent::class)
abstract class TLWorkSpaceModule {

    @Binds
    abstract fun bindTLWorkSpaceHomeScreenRepository(
        repository: TLWorkSpaceHomeScreenRepositoryImpl
    ): TLWorkSpaceHomeScreenRepository

    @Binds
    abstract fun bindTLWorkspaceUpcomingGigersRepository(
        repository: TLWorkspaceUpcomingGigersRepositoryImpl
    ): TLWorkspaceUpcomingGigersRepository


    @Binds
    abstract fun bindTlWorkSpaceRemoteDataStore(
        store: TlWorkSpaceRemoteDataStoreImpl
    ): TlWorkSpaceRemoteDataStore

    @Binds
    abstract fun bindTlWorkSpaceLocalDataStore(
        store: TlWorkSpaceLocalDataStoreImpl
    ): TlWorkSpaceLocalDataStore

    @Binds
    abstract fun bindTlWorkspaceUpcomingGigersRemoteDatastore(
        store: TlWorkspaceUpcomingGigersRemoteDatastoreImpl
    ): TlWorkspaceUpcomingGigersRemoteDatastore

    @Binds
    abstract fun bindTLWorkspaceComplianceRepository(
        repository: TLWorkspaceComplianceRepositoryImpl
    ): TLWorkspaceComplianceRepository

    @Binds
    abstract fun bindTLWorkspaceRetentionRepository(
        repository: TLWorkspaceRetentionRepositoryImpl
    ): TLWorkspaceRetentionRepository






    companion object {

        @Provides
        fun provideTLWorkSpaceService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): TLWorkSpaceService {
            return retrofitServiceFactory.prepareService(TLWorkSpaceService::class.java)
        }

        @Provides
        fun provideTLWorkUpcomingGigersService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): TLWorkUpcomingGigersService {
            return retrofitServiceFactory.prepareService(TLWorkUpcomingGigersService::class.java)
        }
    }
}