package com.gigforce.app.di

import com.gigforce.app.di.implementations.MyViewHolderFactory
import com.gigforce.app.nav.NavManagerImpl
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_app.IMainNavDataRepository
import com.gigforce.giger_app.MainNavDataRepository
import com.gigforce.giger_app.vm.HomeCardsFBRepository
import com.gigforce.giger_app.vm.IHomeCardsFBRepository
import com.gigforce.learning.ILearningDataRepository
import com.gigforce.learning.LearningDataRepository
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
    fun provideManNavRepo(imp: LearningDataRepository): ILearningDataRepository
}

@InstallIn(ViewModelComponent::class)
@Module
interface ViewModelBindings {

    @Binds
    fun provideHomeMenusRepo(imp: HomeCardsFBRepository): IHomeCardsFBRepository
}