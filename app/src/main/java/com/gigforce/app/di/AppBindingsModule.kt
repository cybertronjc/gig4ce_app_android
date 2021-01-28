package com.gigforce.app.di

import com.gigforce.app.di.implementations.ChatNavigationImpl
import com.gigforce.app.di.implementations.MyViewHolderFactory
import com.gigforce.app.di.implementations.NavManagerImpl
import com.gigforce.core.ICoreViewHolderFactory
import com.gigforce.core.navigation.INavigation
import com.gigforce.modules.feature_chat.core.IChatNavigation
import dagger.Module
import dagger.Provides

@Module
class AppBindingsModule {

    @Provides
    fun provideNavigation(): IChatNavigation = ChatNavigationImpl()

    @Provides
    fun provideNavigationBinding(): INavigation = NavManagerImpl()

    @Provides
    fun iViewTypeFinderProvider(): ICoreViewHolderFactory = MyViewHolderFactory()

}