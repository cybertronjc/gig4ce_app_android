package com.gigforce.app.di

import com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending.TLWorkCompliancePendingService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TlWorkSpaceLocalDataStore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.TlWorkspaceUpcomingGigersRemoteDatastore
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TLWorkSpaceService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.TLWorkUpcomingGigersService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.compliance_pending.TLWorkspaceComplianceRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.home_screen.TLWorkSpaceHomeScreenRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.payout.PayoutRetrofitService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.payout.TLWorkspaceGigerPayoutRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.retention.RetentionRetrofitService
import com.gigforce.app.data.repositoriesImpl.tl_workspace.retention.TLWorkspaceRetentionRepositoryImpl
import com.gigforce.app.data.repositoriesImpl.tl_workspace.upcoming_gigers.TLWorkspaceUpcomingGigersRepositoryImpl
import com.gigforce.app.domain.repositories.tl_workspace.*
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
    abstract fun bindTLWorkSpaceHomeScreenRepository(
        repository: TLWorkSpaceHomeScreenRepositoryImpl
    ): TLWorkSpaceHomeScreenRepository

    @Binds
    abstract fun bindTLWorkspaceUpcomingGigersRepository(
        repository: TLWorkspaceUpcomingGigersRepositoryImpl
    ): TLWorkspaceUpcomingGigersRepository


    @Binds
    abstract fun bindTLWorkspaceComplianceRepository(
        repository: TLWorkspaceComplianceRepositoryImpl
    ): TLWorkspaceComplianceRepository

    @Binds
    abstract fun bindTLWorkspaceRetentionRepository(
        repository: TLWorkspaceRetentionRepositoryImpl
    ): TLWorkspaceRetentionRepository

    @Binds
    abstract fun bindTLWorkspaceGigerPayoutRepository(
        repository: TLWorkspaceGigerPayoutRepositoryImpl
    ): TLWorkspacePayoutRepository


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

        @Provides
        fun provideTLWorkspaceRetentionRetrofitService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): RetentionRetrofitService {
            return retrofitServiceFactory.prepareService(RetentionRetrofitService::class.java)
        }

        @Provides
        fun provideTLWorkCompliancePendingService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): TLWorkCompliancePendingService {
            return retrofitServiceFactory.prepareService(TLWorkCompliancePendingService::class.java)
        }


        @Provides
        fun provideTLWorkspaceGigerPayoutRetrofitService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): PayoutRetrofitService {
            return retrofitServiceFactory.prepareService(PayoutRetrofitService::class.java)
        }


    }
}