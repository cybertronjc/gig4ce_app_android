package com.gigforce.app.notification

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.TaskStackBuilder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.gigforce.app.MainActivity
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.extensions.toBundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.moengage.firebase.MoEFireBaseHelper
import com.moengage.pushbase.MoEPushHelper
import java.util.*
import kotlin.random.Random


class MyFirebaseMessagingService : FirebaseMessagingService() {

    val TAG: String = "Firebase/FCM"
    var fcmToken: String? = null

    private val notificationHelper: NotificationHelper by lazy {
        NotificationHelper(applicationContext)
    }

    private val moEngagePushedHelper: MoEPushHelper by lazy {
        MoEPushHelper.getInstance()
    }

    private val currentUser: FirebaseUser?
        get() {
            return FirebaseAuth.getInstance().currentUser
        }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.v(TAG, "[On New Token] Firebase Received: " + token)
        this.fcmToken = token
        this.registerFirebaseToken(token)
    }

    private fun registerFirebaseToken(token: String) {
        registerTokenOnMoEngage(token)

        FirebaseAuth.getInstance().addAuthStateListener {
            it.currentUser?.let {
                FirebaseFirestore.getInstance().collection("firebase_tokens").document(token)
                    .set(
                        hashMapOf(
                            "uid" to it.uid,
                            "type" to "fcm",
                            "timestamp" to Date().time
                        )
                    ).addOnSuccessListener {
                        Log.v(TAG, "Token Updated on Firestore Successfully")
                    }.addOnFailureListener {
                        Log.e(TAG, "Token Update Failed on Firestore", it)
                        CrashlyticsLogger.e(
                            "MyFirebaseMessagingService",
                            "Token Update Failed on Firestore",
                            it
                        )
                    }


            } ?: run {
                Log.v(
                    TAG,
                    "User Not Authenticated. Ideally set an Auth Listener and Register when Authenticated"
                )
            }
        }
    }

    private fun registerTokenOnMoEngage(token: String) {
        try {
            MoEFireBaseHelper.getInstance().passPushToken(applicationContext, token)
        } catch (e: Exception) {
            Log.e(TAG, "Token Update Failed on MoEngage")
            CrashlyticsLogger.e(
                "MyFirebaseMessagingService",
                "Token Update Failed on MoEngage",
                e
            )
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Notification received")

        if (NotificationHelper.isSilentPush(remoteMessage.data)) {
            notificationHelper.handleSilentPush(applicationContext, remoteMessage.data)
            return
        } else if (moEngagePushedHelper.isFromMoEngagePlatform(remoteMessage.data)) {
            MoEPushHelper.getInstance()
                .logNotificationReceived(applicationContext, remoteMessage.data)

            if (moEngagePushedHelper.isSilentPush(remoteMessage.data)) {
                return
            }
            MoEFireBaseHelper.getInstance().passPushPayload(applicationContext, remoteMessage.data)
        } else {
            handleNotificationMessageNotFromMoEngage(remoteMessage)
        }
    }

    private fun handleNotificationMessageNotFromMoEngage(remoteMessage: RemoteMessage) {
        Log.d(TAG, "From: ${remoteMessage.from}")

        remoteMessage.notification?.let {
            Log.d(TAG, "Message Notification Body: ${it.body}")

            /**
             * Receiver id is uid of user for whom message was sent,
             * if message was received with delay and logged in user was changed, message is not shown
             */
            val isForCurrentUserId =
                remoteMessage.data.getOrDefault(RECEIVER_ID, "") == currentUser?.uid
            if (!isForCurrentUserId) {
                Log.d(
                    TAG,
                    "Message Notification Received but receiver id did not match with current user id ${remoteMessage.data}"
                )
                return
            }

            val isChatMessage = remoteMessage.data.getOrDefault(IS_CHAT_MESSAGE, "false") == "true"
            if (isChatMessage) {

                val intent =
                    Intent(NotificationConstants.BROADCAST_ACTIONS.SHOW_CHAT_NOTIFICATION).apply {
                        putExtra(INTENT_EXTRA_REMOTE_MESSAGE, remoteMessage)
                    }
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
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
        const val RECEIVER_ID = "receiver_id"
        const val CHANNEL_ID_CHAt = "chat_messages"

        const val INTENT_EXTRA_REMOTE_MESSAGE = "remote_message"

    }
}