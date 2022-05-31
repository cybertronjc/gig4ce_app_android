package com.gigforce.app.di

import com.gigforce.giger_gigs.gighistory.DataCallbacks
import com.gigforce.giger_gigs.gighistory.GigHistoryRepository
import dagger.Binds
import dagger.Module
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