package com.gigforce.app.di

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TLWorkSpaceModule {

    companion object {

//        @Provides
//        fun providePayoutRetrofitServiceService(
//            retrofitServiceFactory: RetrofitServiceFactory
//        ): PayoutRetrofitService {
//            return retrofitServiceFactory.prepareService(PayoutRetrofitService::class.java)
//        }
    }
}