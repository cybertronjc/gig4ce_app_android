package com.gigforce.app

import android.app.Application
import android.app.NotificationManager
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.core.ILoginInfoProvider
import com.gigforce.core.LoginInfo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApplication : Application(),
    ILoginInfoProvider
{

    override fun onCreate() {
        super.onCreate()
        setupCleverTap()
    }

    private fun setupCleverTap() {
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

        cleverTapAPI?.pushEvent("MAIN_APP_CREATED")
    }

    var loginInfo: LoginInfo = LoginInfo()

    override fun provideLoginInfo(): LoginInfo {
        return loginInfo
    }

    fun setupLoginInfo() {
        FirebaseAuth.getInstance().addAuthStateListener {
            it.currentUser?.let {
                loginInfo.isLoggedIn = true
                loginInfo.uid = it.uid

                FirebaseFirestore.getInstance().collection("Profile").document(it.uid)
                    .addSnapshotListener(
                        EventListener<DocumentSnapshot> { value, e ->
                            if (e != null) {
                                return@EventListener
                            }
                            value?.let {
                                val obj = it.toObject(ProfileData::class.java)
                                obj?.let {
                                    loginInfo.profileName = it.name
                                    loginInfo.profilePicPath = it.profileAvatarThumbnail
                                }
                            }
                        })
            }

            it.currentUser ?: let {
                loginInfo.onSignOut()
            }
        }
    }
}