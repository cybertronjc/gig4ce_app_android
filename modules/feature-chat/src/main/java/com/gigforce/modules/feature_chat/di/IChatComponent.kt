package com.gigforce.modules.feature_chat.di

import com.gigforce.modules.feature_chat.ui.ChatListItem
import dagger.Subcomponent

@Subcomponent(modules = [ChatModule::class])
interface IChatComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): IChatComponent
    }

    // Classes that can be injected by this Component
    fun inject(item: ChatListItem)
}