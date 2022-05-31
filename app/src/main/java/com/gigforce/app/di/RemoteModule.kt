package com.gigforce.app.di

import com.gigforce.app.data.repositoriesImpl.gigs.GigService
import com.gigforce.common_ui.remote.PayoutRetrofitService
import com.gigforce.core.retrofit.RetrofitServiceFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RemoteModule {

    companion object {

        @Provides
        fun providePayoutRetrofitServiceService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): PayoutRetrofitService {
            return retrofitServiceFactory.prepareService(PayoutRetrofitService::class.java)
        }

        @Provides
        fun provideGigRetrofitService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): GigService {
            return retrofitServiceFactory.prepareService(GigService::class.java)
        }
    }
}