package com.gigforce.app.modules.notifications

import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class MyFirebaseMessagingService: FirebaseMessagingService() {

    val TAG:String = "Firebase/FCM"
    var fcmToken:String? = null

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.v(TAG, "[On New Token] Firebase Received: " + token)
        this.fcmToken = token
        this.registerFirebaseToken(token)
    }

    private fun registerFirebaseToken(token: String) {
        // doing nothing for now

        RegisterFirebaseTokenIfLoggedIn()

        FirebaseAuth.getInstance().currentUser ?:let {
            Log.v(TAG, "User Not Authenticated. Ideally set an Auth Listener and Register when Authenticated")
            FirebaseAuth.getInstance().addAuthStateListener {
                Log.v(TAG, "Firebase Auth State Changed")
                RegisterFirebaseTokenIfLoggedIn()
            }
        }
    }

    private fun RegisterFirebaseTokenIfLoggedIn(){
        FirebaseAuth.getInstance().currentUser ?.let {
            val uid = it.uid
            FirebaseFirestore.getInstance().collection("firebase_tokens").document(this.fcmToken!!).set(
                    hashMapOf(
                            "uid" to uid,
                            "type" to "fcm",
                            "timestamp" to Date().time
                    )
            ).addOnSuccessListener {
                Log.v(TAG, "Token Updated on Firestore Successfully")
            }.addOnFailureListener {
                Log.e(TAG, "Token Update Failed on Firestore", it)
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                // scheduleJob()
            } else {
                // Handle message within 10 seconds
                // handleNow()
            }
        }

        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }
}