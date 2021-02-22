package com.gigforce.app.di

import com.gigforce.app.di.implementations.MyViewHolderFactory
import com.gigforce.app.nav.NavManagerImpl
import com.gigforce.client_activation.repo.ClientActivationDataRepository
import com.gigforce.client_activation.repo.IClientActivationDataRepository
import com.gigforce.common_ui.ILoginInfoRepo
import com.gigforce.common_ui.LoginInfoRepo
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.repo.*
import com.gigforce.learning.repo.ILearningDataRepository
import com.gigforce.learning.repo.LearningDataRepository
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

    @Binds
    fun provideManNavRepo(imp: MainNavDataRepository): IMainNavDataRepository

    @Binds
    fun provideLearningRepo(imp: LearningDataRepository): ILearningDataRepository

    @Binds
    fun provideClientActivationRepo(imp: ClientActivationDataRepository): IClientActivationDataRepository

    @Binds
    fun provideHelpVideosRepo(imp: HelpVideosDataRepository): IHelpVideosDataRepository

    @Binds
    fun provideUpcomingGigsRepo(imp: UpcomingGigInfoRepository): IUpcomingGigInfoRepository

    @Binds
    fun provideLoginInfo(imp: LoginInfoRepo): ILoginInfoRepo
}

@InstallIn(ViewModelComponent::class)
@Module
interface ViewModelBindings {

    @Binds
    fun provideHomeMenusRepo(imp: HomeCardsFBRepository): IHomeCardsFBRepository

    @Binds
    fun provideBSDataRepo(imp: BSDataRepository): IBSDataRepository
}