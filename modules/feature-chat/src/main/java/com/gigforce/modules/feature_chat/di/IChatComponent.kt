package com.gigforce.modules.feature_chat.di

import com.gigforce.core.IViewTypeFinder
import com.gigforce.modules.feature_chat.adapters.ChatAdapter
import com.gigforce.modules.feature_chat.adapters.ViewTypeFinder
import com.gigforce.modules.feature_chat.ui.ChatListItem
import dagger.Binds
import dagger.Component
import dagger.Subcomponent

@Subcomponent(modules = [ChatModule::class])
interface IChatComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): IChatComponent
    }

    // Classes that can be injected by this Component
    fun inject(item: ChatListItem)

    fun inject(item: ChatAdapter)
}