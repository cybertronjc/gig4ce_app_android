package com.gigforce.app.di

import android.content.Context
import com.gigforce.modules.feature_chat.ui.ChatListItem
import dagger.BindsInstance
import dagger.Component

@Component(modules = [ChatModule::class])
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): AppComponent
    }

    // Classes that can be injected by this Component
    fun inject(item: ChatListItem)
}