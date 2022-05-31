package com.gigforce.app.di

import android.content.Context
import android.content.SharedPreferences
import com.gigforce.app.data.repositoriesImpl.gigs.GigerAttendanceService
import com.gigforce.app.di.implementations.BuildConfigImp
import com.gigforce.common_ui.remote.*
import com.gigforce.common_ui.remote.verification.VerificationKycService
import com.gigforce.core.CoreConstants
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.retrofit.GeneratePaySlipService
import com.gigforce.core.retrofit.RetrofitServiceFactory
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_app.help.HelpSectionService
import com.gigforce.giger_gigs.tl_login_details.LoginSummaryService
import com.gigforce.giger_gigs.travelling_info.TravellingService
import com.gigforce.modules.feature_chat.repositories.DownloadChatAttachmentService
import com.gigforce.modules.feature_chat.service.SyncPref
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SingeltonBindings {

    @Binds
    abstract fun provideBuildConfig(imp: BuildConfigImp): IBuildConfig

    companion object {

        @Provides
        fun provideGlobalSharedPreferences(
            @ApplicationContext appContext: Context
        ): SharedPreferences {
            return appContext.getSharedPreferences(
                CoreConstants.SHARED_PREFERENCE_DB,
                Context.MODE_PRIVATE
            )!!
        }

        //Base
        @Provides
        fun provideFirebaseFirestore(): FirebaseFirestore {
            return FirebaseFirestore.getInstance()
        }

        @Provides
        fun provideFirebaseStorage(): FirebaseStorage {
            return FirebaseStorage.getInstance()
        }

        @Singleton
        @Provides
        fun provideFirebaseRemoteConfig() : FirebaseRemoteConfig{
            return FirebaseRemoteConfig.getInstance()
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

        @Provides
        fun provideSignatureImageService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): SignatureImageService {
            return retrofitServiceFactory.prepareService(SignatureImageService::class.java)
        }

        @Provides
        fun provideAuthService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): AuthService {
            return retrofitServiceFactory.prepareService(AuthService::class.java)
        }

        @Provides
        fun provideKYCService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): VerificationKycService {
            return retrofitServiceFactory.prepareService(VerificationKycService::class.java)
        }

        @Provides
        fun provideDownloadChatAttachmentService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): DownloadChatAttachmentService {
            return retrofitServiceFactory.prepareService(DownloadChatAttachmentService::class.java)
        }

        @Provides
        fun provideGigerAttendanceService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): GigerAttendanceService {
            return retrofitServiceFactory.prepareService(GigerAttendanceService::class.java)
        }

        @Provides
        fun provideHelpSectionService(
            retrofitServiceFactory : RetrofitServiceFactory
        ): HelpSectionService {
            return retrofitServiceFactory.prepareService(HelpSectionService::class.java)
        }

        @Provides
        fun provideTravellingService(
            retrofitServiceFactory: RetrofitServiceFactory
        ): TravellingService {
            return retrofitServiceFactory.prepareService(TravellingService::class.java)
        }

        @Singleton
        @Provides
        fun provideSyncPref(
            @ApplicationContext context: Context
        ): SyncPref {
            return SyncPref.getInstance(context)
        }



        @Singleton
        @Provides
        @Named("session_independent_pref")
        @JvmStatic
        fun provideSessionIndependentPreferences(@ApplicationContext context: Context): SharedPreferences {
            // Returns a [SharedPreferences] which will be not be cleared when user logs out,
            // great for storing things like language selected, app intro shown etc
            return context.getSharedPreferences("session_independent_pref", Context.MODE_PRIVATE)
        }

        @Singleton
        @Provides
        @Named("session_dependent_pref")
        @JvmStatic
        fun provideSessionDependentPreferences(@ApplicationContext context: Context): SharedPreferences {
            // Returns a [SharedPreferences] which will be cleared when user logs out,
            // great for storing things like Current users related data
            return context.getSharedPreferences("session_dependent_pref", Context.MODE_PRIVATE)
        }
    }
}