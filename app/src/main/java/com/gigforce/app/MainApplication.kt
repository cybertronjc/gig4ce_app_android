package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.di.AppComponent
import com.gigforce.app.di.DaggerAppComponent
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.di.IChatComponent


class MainApplication: Application(),
        ChatModuleProvider
{

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    override fun provideChatModule(): IChatComponent {
        return appComponent.createChatComponent().create()
    }

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
    }

    private fun setupCleverTap(){
        val clevertapDefaultInstance =
            CleverTapAPI.getDefaultInstance(applicationContext)

        val cleverTapAPI = CleverTapAPI.getDefaultInstance(applicationContext)
        CleverTapAPI.createNotificationChannel(
            applicationContext,
            "gigforce-general",
            "Gigforce",
            "Gigforce Push Notifications",
            NotificationManager.IMPORTANCE_MAX,
            true
        )

        cleverTapAPI?.pushEvent("MAIN_APP_CREATED");
    }



}