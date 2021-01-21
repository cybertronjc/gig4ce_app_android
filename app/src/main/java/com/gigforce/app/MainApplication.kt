package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.di.AppComponent
import com.gigforce.app.di.DaggerAppComponent
import com.gigforce.core.di.CoreComponentProvider
import com.gigforce.core.di.ICoreComponent
import com.gigforce.modules.feature_chat.di.ChatModuleProvider
import com.gigforce.modules.feature_chat.di.IChatComponent
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MainApplication: Application(),
        ChatModuleProvider,
        CoreComponentProvider
{

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(applicationContext)
    }

    override fun provideChatModule(): IChatComponent {
        return appComponent.createChatComponent().create()
    }

    override fun provide(): ICoreComponent {
        return appComponent.createCoreComponent().create()
    }

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
        ProcessLifecycleOwner.get().lifecycle.addObserver(PresenceManager())
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