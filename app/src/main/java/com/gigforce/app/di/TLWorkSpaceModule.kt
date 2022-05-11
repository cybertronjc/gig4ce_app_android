package com.gigforce.app.di

import com.gigforce.app.data.local.local_data_store_Implementations.tl_workspace.TlWorkSpaceLocalDataStoreImpl
import com.gigforce.app.data.remote.datastoreImpls.tl_workspace.TlWorkSpaceRemoteDataStoreImpl
import com.gigforce.app.data.remote.retrofitServices.TLWorkSpaceService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TLWorkSpaceHomeScreenRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceLocalDataStore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceRemoteDataStore
import com.gigforce.app.domain.repositories.tl_workspace.TLWorkSpaceHomeScreenRepository
import com.gigforce.core.retrofit.RetrofitServiceFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TLWorkSpaceModule {

    @Binds
    abstract fun bindTLWorkSpaceHomeScreenRepository(repository: TLWorkSpaceHomeScreenRepositoryImpl): TLWorkSpaceHomeScreenRepository

    @Binds
    abstract fun bindTlWorkSpaceRemoteDataStore(store: TlWorkSpaceRemoteDataStoreImpl): TlWorkSpaceRemoteDataStore

    @Binds
    abstract fun bindTlWorkSpaceLocalDataStore(store: TlWorkSpaceLocalDataStoreImpl): TlWorkSpaceLocalDataStore


    companion object {

        @Provides
        fun providePayoutRetrofitServiceService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): TLWorkSpaceService {
            return retrofitServiceFactory.prepareService(TLWorkSpaceService::class.java)
        }
    }
}