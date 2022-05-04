package com.gigforce.app.di

import com.gigforce.app.di.implementations.BuildConfigImp
import com.gigforce.common_ui.remote.GigService
import com.gigforce.common_ui.remote.PayoutRetrofitService
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.retrofit.RetrofitServiceFactory
import com.gigforce.giger_gigs.gighistory.DataCallbacks
import com.gigforce.giger_gigs.gighistory.GigHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class GigModule {

    @Binds
    abstract fun provideBuildConfig(gigHistoryRepository: GigHistoryRepository): DataCallbacks


    companion object {

    }
}