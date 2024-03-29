package com.gigforce.app.di

import com.gigforce.app.di.implementations.*
import com.gigforce.app.nav.NavManagerImpl
import com.gigforce.client_activation.client_activation.repository.AadhaarDetailsRepository
import com.gigforce.client_activation.repo.ClientActivationDataRepository
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.AppDialogsInterface
import com.gigforce.common_ui.IUserInfo
import com.gigforce.common_ui.UserInfoImp
import com.gigforce.common_ui.repository.BannerCardRepository
import com.gigforce.common_ui.repository.IBannerCardRepository
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.repository.repo.ILearningDataRepository
import com.gigforce.common_ui.repository.repo.LearningDataRepository
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.shareddata.SharedPreAndCommonUtilInterface
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.di.interfaces.INavHost
import com.gigforce.core.di.repo.IAadhaarDetailsRepository
import com.gigforce.core.di.repo.IProfileFirestoreRepository
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.repo.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ViewModelComponent

@InstallIn(ActivityComponent::class)
@Module
interface AppBindingsModule {

    @Binds
    fun iViewTypeFinderProvider(impl: MyViewHolderFactory): ICoreViewHolderFactory

    @Binds
    fun provideNavigationBinding(impl: NavManagerImpl): INavigation

//    @Binds
//    fun provideManNavRepo(imp: MainNavDataRepository): IMainNavDataRepository

    @Binds
    fun provideLearningRepo(imp: LearningDataRepository): ILearningDataRepository

    @Binds
    fun provideClientActivationRepo(imp: ClientActivationDataRepository): IClientActivationDataRepository

    @Binds
    fun provideHelpVideosRepo(imp: HelpVideosDataRepository): IHelpVideosDataRepository

    @Binds
    fun provideUpcomingGigsRepo(imp: UpcomingGigInfoRepository): IUpcomingGigInfoRepository


    @Binds
    fun provideEventTracker(imp: EventTrackerImp): IEventTracker

    @Binds
    fun provideCommonDialogs(imp: AppDialogsImp): AppDialogsInterface

    @Binds
    fun provideCommonUtil(imp: SharedPreAndCommonUtilDataImp): SharedPreAndCommonUtilInterface

    @Binds
    fun provideLoginInfo(imp: UserInfoImp): IUserInfo

    @Binds
    fun provideNavHost(imp: NavHostImp): INavHost

    @Binds
    fun provideBannerCardRepository(repo: BannerCardRepository): IBannerCardRepository

}

@InstallIn(ViewModelComponent::class)
@Module
interface ViewModelBindings {

    @Binds
    fun provideHomeMenusRepo(imp: HomeCardsFBRepository): IHomeCardsFBRepository

    @Binds
    fun provideBSDataRepo(imp: BSDataRepository): IBSDataRepository

    @Binds
    fun privideProfileRepo(imp: ProfileFirebaseRepository): IProfileFirestoreRepository

    @Binds
    fun provideBuildVMConfig(imp: BuildConfigVMImp): IBuildConfigVM

    @Binds
    fun provideViewModelEventTracker(imp: EventTrackerImp): IEventTracker

    @Binds
    fun provideAadhaarDetailsRepository(imp: AadhaarDetailsRepository): IAadhaarDetailsRepository

    @Binds
    fun provideMainNavRepo(imp: MainNavDataRepository): IMainNavDataRepository

    @Binds
    fun provideCommonUtilVM(imp: FeatureIconsDataRepository): IFeatureIconsDataRepository

    @Binds
    fun provideClientActivationRepo(imp: ClientActivationDataRepository): IClientActivationDataRepository

    @Binds
    fun provideLearningRepo(imp: LearningDataRepository): ILearningDataRepository

    @Binds
    fun provideHelpVideosRepo(imp: HelpVideosDataRepository): IHelpVideosDataRepository

    @Binds
    fun provideSharedData(imp: SharedPreAndCommonUtilDataImp): SharedPreAndCommonUtilInterface

    @Binds
    fun provideUpcomingGigsRepo(imp: UpcomingGigInfoRepository): IUpcomingGigInfoRepository
}



