package com.gigforce.app.di

import com.gigforce.core.di.ICoreComponent
import com.gigforce.modules.feature_chat.di.IChatComponent
import dagger.Module

@Module(subcomponents = [IChatComponent::class, ICoreComponent::class])
class AppSubcomponents {
}