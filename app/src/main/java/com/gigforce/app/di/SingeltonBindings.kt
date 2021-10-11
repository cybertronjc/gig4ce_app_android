package com.gigforce.app.di

import com.gigforce.app.di.implementations.BuildConfigImp
import com.gigforce.common_ui.remote.JoiningProfileService
import com.gigforce.common_ui.remote.ProfileCommonService
import com.gigforce.common_ui.remote.ReferralService
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.retrofit.GeneratePaySlipService
import com.gigforce.core.retrofit.RetrofitFactory
import com.gigforce.core.retrofit.RetrofitServiceFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.tl_login_details.LoginSummaryService
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SingeltonBindings {

    @Binds
    abstract fun provideBuildConfig(imp: BuildConfigImp): IBuildConfig

    companion object {

        //Base
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        fun provideFirebaseAuthStateListener(): FirebaseAuthStateListener {
            return FirebaseAuthStateListener.getInstance()
        }

        @Provides
        fun provideLogger(): GigforceLogger {
            return GigforceLogger()
        }

        //Remote Services

        @Provides
        fun provideJoiningProfileService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): JoiningProfileService {
            return retrofitServiceFactory.prepareService(JoiningProfileService::class.java)
        }

        @Provides
        fun provideReferralService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): ReferralService {
            return retrofitServiceFactory.prepareService(ReferralService::class.java)
        }

        @Provides
        fun provideTlReportService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): LoginSummaryService {
            return retrofitServiceFactory.prepareService(LoginSummaryService::class.java)
        }

        @Provides
        fun provideGeneratePaySlipService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): GeneratePaySlipService {
            return retrofitServiceFactory.prepareService(GeneratePaySlipService::class.java)
        }

        @Provides
        fun provideProfileCommonService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): ProfileCommonService {
            return retrofitServiceFactory.prepareService(ProfileCommonService::class.java)
        }
    }
}