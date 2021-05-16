package com.gigforce.app.notification

import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.clevertap.android.sdk.CleverTapAPI
import com.gigforce.app.MainActivity
import com.gigforce.core.extensions.toBundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*
import kotlin.random.Random


class MyFirebaseMessagingService : FirebaseMessagingService() {

    val TAG: String = "Firebase/FCM"
    var fcmToken: String? = null

    private val chatNotificationHandler: ChatNotificationHandler by lazy {
        ChatNotificationHandler(applicationContext)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.v(TAG, "[On New Token] Firebase Received: " + token)
        this.fcmToken = token
        this.registerFirebaseToken(token)
    }

    private fun registerFirebaseToken(token: String) {
        // doing nothing for now

        registerFirebaseTokenIfLoggedIn()
        FirebaseAuth.getInstance().currentUser ?: let {
            Log.v(
                    TAG,
                    "User Not Authenticated. Ideally set an Auth Listener and Register when Authenticated"
            )
            FirebaseAuth.getInstance().addAuthStateListener {
                Log.v(TAG, "Firebase Auth State Changed")
                registerFirebaseTokenIfLoggedIn()
            }
        }
    }

    private fun registerFirebaseTokenIfLoggedIn() {
        FirebaseAuth.getInstance().currentUser?.let {
            val uid = it.uid
            FirebaseFirestore.getInstance().collection("firebase_tokens").document(this.fcmToken!!)
                .set(
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

        try {
            if (remoteMessage.data.isNotEmpty()) {
                val extras = Bundle()
                for ((key, value) in remoteMessage.data.entries) {
                    extras.putString(key, value)
                }
                val info = CleverTapAPI.getNotificationInfo(extras)
                if (info.fromCleverTap) {
                    CleverTapAPI.createNotification(applicationContext, extras)
                } else {
                    // not from CleverTap handle yourself or pass to another provider
                    handleNotificationMessageNotFromCleverTap(remoteMessage)
                }
            }
        } catch (t: Throwable) {
            Log.d("MYFCMLIST", "Error parsing FCM message", t)
        }



        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    private fun handleNotificationMessageNotFromCleverTap(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")
        // Check if message contains a notification payload.
        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")

            val isChatMessage = remoteMessage.data.getOrDefault(IS_CHAT_MESSAGE, "false") == "true"
            if (isChatMessage) {

                val intent = Intent(NotificationConstants.BROADCAST_ACTIONS.SHOW_CHAT_NOTIFICATION).apply {
                    putExtra(INTENT_EXTRA_REMOTE_MESSAGE, remoteMessage)
                }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
                //handleChatNotifications(remoteMessage)
            } else {

                val pendingIntent = if (remoteMessage.data.isNotEmpty()) {
                    if (it.clickAction != null)
                        createPendingIntentFromData(it.clickAction!!, remoteMessage.data)
                    else
                        null
                } else
                    null

                NotificationHelper(applicationContext)
                        .createUrgentPriorityNotification(
                                title = it.title ?: "Gigforce",
                                message = it.body ?: "message",
                                pendingIntent = pendingIntent
                        )

            }
        }
    }


    private fun createPendingIntentFromData(
            clickAction: String,
            data: Map<String, String>
    ): PendingIntent? {

        val dataBundle = data.toBundle()
        dataBundle.putString(NotificationConstants.INTENT_EXTRA_CLICK_ACTION, clickAction)

        return TaskStackBuilder.create(applicationContext).run {
            addNextIntentWithParentStack(
                    Intent(
                            applicationContext,
                            MainActivity::class.java
                    ).apply {
                        putExtras(data.toBundle())
                    })
            val reqCode = Random.nextInt(0, 100)
            getPendingIntent(reqCode, PendingIntent.FLAG_ONE_SHOT)
        }
    }


    companion object {

        const val IS_CHAT_MESSAGE = "is_chat_message"
        const val CHANNEL_ID_CHAt = "chat_messages"

        const val INTENT_EXTRA_REMOTE_MESSAGE = "remote_message"
    }
}