package com.gigforce.app.di

import android.content.Context
import com.gigforce.modules.feature_chat.di.IChatComponent
import dagger.BindsInstance
import dagger.Component

@Component(modules = [ChatModule::class, AppSubcomponents::class])
interface AppComponent {

    // Factory to create instances of the AppComponent
    @Component.Factory
    interface Factory {
        // With @BindsInstance, the Context passed in will be available in the graph
        fun create(@BindsInstance context: Context): AppComponent
    }

    fun createChatComponent(): IChatComponent.Factory
}