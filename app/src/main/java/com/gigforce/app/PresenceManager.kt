package com.gigforce.app

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class PresenceManager: LifecycleObserver {

    init {
        this.setupPresence()
    }

    var usrRef: DatabaseReference? = null
    fun setupPresence(){
        FirebaseAuth.getInstance().addAuthStateListener {
            Log.i("app/auth","Auth State Changed")
            val user = it.currentUser
            user ?. let {
                Log.i("app/auth","Signed In ${user.uid}")

                val database = Firebase.database.reference
                usrRef = Firebase.database.getReference("user_status/${user.uid}/status")
                usrRef?.onDisconnect()?.setValue("offline")

                Firebase.database.getReference(".info/connected").addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val isConnected = snapshot.value as Boolean
                        Log.i("app/auth","data changed To ${isConnected}")
                        usrRef?.setValue(if(isConnected) "online" else "offline")
                    }
                })
            }

            user ?: let{
                Log.i("app/auth","Signed Out")
                usrRef ?. let {
                    usrRef ?. setValue("offline")
                    usrRef = null
                }
            }
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onBackground(){
        FirebaseAuth.getInstance().currentUser ?. let {
            val usrRef = Firebase.database.getReference("user_status/${it.uid}/status")
            usrRef.setValue("offline")
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onForeGround(){
        FirebaseAuth.getInstance().currentUser ?. let {
            val usrRef = Firebase.database.getReference("user_status/${it.uid}/status")
            usrRef.setValue("online")
        }
    }

}