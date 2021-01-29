package com.gigforce.app.di

import com.gigforce.app.di.implementations.MyViewHolderFactory
import com.gigforce.app.di.implementations.NavManagerImpl
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.navigation.INavigation
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.components.FragmentComponent

@InstallIn(ActivityComponent::class)
@Module
interface AppBindingsModule {

    @Binds
    fun iViewTypeFinderProvider(impl: MyViewHolderFactory): ICoreViewHolderFactory

    @Binds
    fun provideNavigationBinding(impl:NavManagerImpl): INavigation
}