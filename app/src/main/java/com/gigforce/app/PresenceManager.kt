package com.gigforce.app

import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
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

    var usrRef: DatabaseReference? = null
    fun setupPresence() {
        FirebaseAuth.getInstance().addAuthStateListener {
            Log.i("app/auth", "Auth State Changed")
            val user = it.currentUser
            user?.let {
                Log.i("app/auth", "Signed In ${user.uid}")

                usrRef = Firebase.database.getReference("user_status/${user.uid}")
                usrRef?.onDisconnect()?.setValue(
                    PresenceData(
                        status = OFFLINE,
                        lastUpdatedOn = Date().time,
                        appStatus = APP_STATUS_DISCONNECTED
                    )
                )

                Firebase.database.getReference(".info/connected")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onCancelled(error: DatabaseError) {

                        }

                        override fun onDataChange(snapshot: DataSnapshot) {
                            val isConnected = snapshot.value as Boolean
                            Log.i("app/auth", "data changed To ${isConnected}")
                            val presenceData = PresenceData(
                                status = if (isConnected) ONLINE else OFFLINE,
                                lastUpdatedOn = Date().time,
                                appStatus = if (isConnected) APP_STATUS_ACTIVE else APP_STATUS_INACTIVE
                            )

                            usrRef?.setValue(presenceData)
                        }
                    })
            }

            user ?: let {
                Log.i("app/auth", "Signed Out")
                usrRef?.let {
                    usrRef?.setValue(
                        PresenceData(
                            status = OFFLINE,
                            lastUpdatedOn = Date().time,
                            appStatus = APP_STATUS_SIGNED_OUT
                        )
                    )
                    usrRef = null
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onBackground() {
        FirebaseAuth.getInstance().currentUser?.let {
            usrRef?.setValue(
                PresenceData(
                    status = OFFLINE,
                    lastUpdatedOn = Date().time,
                    appStatus = APP_STATUS_INACTIVE
                )
            )
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onForeGround() {
        FirebaseAuth.getInstance().currentUser?.let {
            usrRef?.setValue(
                PresenceData(
                    status = ONLINE,
                    lastUpdatedOn = Date().time,
                    appStatus = APP_STATUS_ACTIVE
                )
            )
        }
    }

}