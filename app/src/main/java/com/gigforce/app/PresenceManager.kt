package com.gigforce.app

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.util.*

@Keep
data class PresenceData(
    var status: String,
    var appStatus: String,
    var webStatus: String,
    var lastUpdatedOn: Long = Date().time
)

class PresenceManager : LifecycleObserver {

    companion object {

        const val ONLINE = "online"
        const val OFFLINE = "offline"

        const val APP_STATUS_ACTIVE = "active"
        const val APP_STATUS_INACTIVE = "background"
        const val APP_STATUS_DISCONNECTED = "disconnected"
        const val APP_STATUS_SIGNED_OUT = "signed_out"
    }

    init {
        this.setupPresence()
    }

    private var usrRef: DatabaseReference? = null

    private fun setupPresence() {
        FirebaseAuth.getInstance().addAuthStateListener {
            Log.i("app/auth", "Auth State Changed")
            usrRef = null

            val user = it.currentUser
            user?.let {
                Log.i("app/auth", "Signed In ${user.uid}")

                usrRef = Firebase.database.getReference("user_status/${user.uid}")

                Firebase.database.getReference(".info/connected")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isConnected = snapshot.value as Boolean
                            Log.i("app/auth", "data changed To ${isConnected}")

                            if(isConnected) {

                                usrRef?.onDisconnect()?.updateChildren(

                                    mapOf(
                                        "status" to OFFLINE,
                                        "lastUpdatedOn" to Date().time,
                                        "appStatus" to APP_STATUS_DISCONNECTED
                                    )
                                )?.continueWith {

                                    usrRef?.updateChildren(
                                        mapOf(
                                            "status" to ONLINE,
                                            "lastUpdatedOn" to Date().time,
                                            "appStatus" to APP_STATUS_ACTIVE
                                        )
                                    )
                                }
                            }
                        }
                    })
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onBackground() {
        usrRef?.updateChildren(
            mapOf(
                "status" to OFFLINE,
                "lastUpdatedOn" to Date().time,
                "appStatus" to APP_STATUS_INACTIVE
            )
        )
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onForeGround() {
        usrRef?.updateChildren(
            mapOf(
                "status" to ONLINE,
                "lastUpdatedOn" to Date().time,
                "appStatus" to APP_STATUS_ACTIVE
            )
        )
    }

}