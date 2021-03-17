package com.gigforce.modules.feature_chat.di

import com.gigforce.modules.feature_chat.screens.ChatHeadersFragment
import com.gigforce.modules.feature_chat.screens.ChatPageFragment
import com.gigforce.modules.feature_chat.screens.ContactsFragment
import com.gigforce.modules.feature_chat.screens.GroupDetailsFragment
import com.gigforce.modules.feature_chat.ui.ChatListItem
import com.gigforce.modules.feature_chat.ui.chatItems.ImageMessageView
import com.gigforce.modules.feature_chat.ui.chatItems.VideoMessageView
import dagger.Subcomponent

@Subcomponent(modules = [ChatModule::class])
interface IChatComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): IChatComponent
    }

    // Classes that can be injected by this Component
    fun inject(item: ChatListItem)

    // Classes that can be injected by this Component
    fun inject(item: ChatHeadersFragment)

    // Classes that can be injected by this Component
    fun inject(item: ChatPageFragment)

    // Classes that can be injected by this Component
    fun inject(item: GroupDetailsFragment)

    // Classes that can be injected by this Component
    fun inject(item: ContactsFragment)

    // Classes that can be injected by this Component
    fun inject(item: ImageMessageView)

    // Classes that can be injected by this Component
    fun inject(item: VideoMessageView)







}