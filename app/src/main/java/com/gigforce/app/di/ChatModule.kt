package com.gigforce.app.di

import com.gigforce.app.di.implementations.ChatNavigationImpl
import com.gigforce.modules.feature_chat.IChatNavigation
import dagger.Binds
import dagger.Module

@Module
abstract class ChatModule {

    @Binds
    abstract fun provideNavigation(chatNavigation:ChatNavigationImpl): IChatNavigation
}