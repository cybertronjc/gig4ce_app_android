package com.gigforce.modules.feature_chat.di

import com.gigforce.core.IViewTypeFinder
import com.gigforce.modules.feature_chat.adapters.ViewTypeFinder
import dagger.Binds
import dagger.Module

@Module
abstract class ChatModule {

    @Binds
    abstract fun test(iViewTypeFinder: ViewTypeFinder):IViewTypeFinder
}