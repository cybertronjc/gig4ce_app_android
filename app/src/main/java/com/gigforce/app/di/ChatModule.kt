package com.gigforce.app.di

import com.gigforce.app.di.implementations.ChatNavigationImpl
import com.gigforce.core.IViewTypeFinder
import com.gigforce.modules.feature_chat.IChatNavigation
import com.gigforce.modules.feature_chat.adapters.ViewTypeFinder
import dagger.Binds
import dagger.Module
import dagger.Provides

@Module
class ChatModule {

    @Provides
    fun provideNavigation(): IChatNavigation = ChatNavigationImpl()

    @Provides
    fun iViewTypeFinderProvider(): IViewTypeFinder = ViewTypeFinder()
}