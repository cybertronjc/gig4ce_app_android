package com.gigforce.app.di

import com.gigforce.app.di.implementations.ChatNavigationImpl
import com.gigforce.app.di.implementations.MyViewHolderFactory
import com.gigforce.app.di.implementations.NavManagerImpl
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.core.IChatNavigation
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
abstract class AppBindingsModule {

    @Binds
    abstract fun provideNavigation(impl:ChatNavigationImpl): IChatNavigation

    @Binds
    abstract fun provideNavigationBinding(impl:NavManagerImpl): INavigation

    @Binds
    abstract fun iViewTypeFinderProvider(impl: MyViewHolderFactory): ICoreViewHolderFactory

}