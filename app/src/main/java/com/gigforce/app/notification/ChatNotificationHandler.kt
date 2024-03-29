package com.gigforce.app.notification

import android.app.Notification
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.TaskStackBuilder
import androidx.core.graphics.drawable.IconCompat
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.toBundle
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*
import javax.inject.Inject
import kotlin.random.Random

class ChatNotificationHandler constructor(
        private val context: Context
) {

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    fun handleChatNotification(
            remoteMessage: RemoteMessage
    ) = GlobalScope.launch {

        val isGroupMessage =
                remoteMessage.data.getOrDefault("is_group_conversation", "false") == "true"
        if (isGroupMessage) {
            handleGroupNotification(remoteMessage)
        } else {
            handleDirectChatMessage(remoteMessage)
        }
    }

    private suspend fun handleDirectChatMessage(
            remoteMessage: RemoteMessage
    ) {
        val person = createPerson(remoteMessage)
        val message = remoteMessage.notification?.body ?: ""
        val messageType = remoteMessage.data.getOrDefault("msg_type", "")
        val fullImagePath = remoteMessage.data.getOrDefault("image", "")
        val chatHeaderId: String = remoteMessage.data.getOrDefault("chat_header_id", "")

        val notificationMessage = NotificationCompat
                .MessagingStyle
                .Message(
                        message,
                        Date().time,
                        person
                )

        if (fullImagePath != null)
            notificationMessage.setData("image/*", Uri.parse(fullImagePath))

        val notificationBuilder =
                NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CHAT_ID)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setColor(Color.parseColor("#000000"))
                        .setStyle(
                                NotificationCompat
                                        .MessagingStyle(person)
                                        .addMessage(notificationMessage)

                        )
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400))

        Log.d("ChatImage", fullImagePath)
        var imageBitmap: Bitmap? = null
        if (fullImagePath.isNotBlank()) {
            imageBitmap = getBitmapFromURL(fullImagePath)
        }

        if (imageBitmap != null) {
            Log.d("ChatImage", "Icon Did set")
            notificationBuilder.setLargeIcon(imageBitmap)
        }

        notificationBuilder.setContentIntent(
                createPendingIntentForChat(
                        NotificationConstants.CLICK_ACTIONS.OPEN_CHAT_PAGE,
                        remoteMessage.data
                )
        )

        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationHelper(context).createUrgentPriorityNotification(
                CHAT_NOTIF_REQUEST_CODE,
                notification

        )

//        var map = mapOf("chat_type" to "Direct", "message_type" to messageType)
//        eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_NOTIFICATION_RECEIVED, map))
    }

    private fun createPendingIntentForChat(
            clickAction: String,
            data: Map<String, String>
    ): PendingIntent? {

        val dataBundle = data.toBundle()
        dataBundle.putString(NotificationConstants.INTENT_EXTRA_CLICK_ACTION, clickAction)
//        dataBundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_ID, dataBundle.getString("sender_id"))
//        dataBundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_NAME, dataBundle.getString("sender_name",""))
//        dataBundle.putString(ChatFragment.INTENT_EXTRA_OTHER_USER_IMAGE, dataBundle.getString("sender_profile",""))
//        dataBundle.putString(ChatFragment.INTENT_EXTRA_CHAT_HEADER_ID, dataBundle.getString("chat_header_id",""))

        return TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(
                    Intent(
                            context,
                            MainActivity::class.java
                    ).apply {
                        putExtras(dataBundle)
                    })
            val reqCode = Random.nextInt(0, 100)
            getPendingIntent(reqCode, PendingIntent.FLAG_ONE_SHOT)
        }
    }


    private suspend fun createPerson(remoteMessage: RemoteMessage): Person {
        val senderName = remoteMessage.notification?.title ?: "User"
        val senderId = remoteMessage.data.getOrDefault("receiver_id", "")
        val senderProfilePicturePath = remoteMessage.data.getOrDefault("imageUrl", "")
        var senderProfilePicture: Bitmap? = null
        if (senderProfilePicturePath != "") {
            //todo
//            val senderProfilePictureUri = firebaseStorage.reference.child(senderProfilePicturePath)
//                .getDownloadUrlOrReturnNull()

//            if (senderProfilePictureUri != null)
            senderProfilePicture = getBitmapFromURL(senderProfilePicturePath.toString())
        }

        return Person.Builder()
                .setIcon(
                        if (senderProfilePicture != null)
                            IconCompat.createWithBitmap(senderProfilePicture)
                        else
                            IconCompat.createWithResource(context, R.drawable.ic_user_2)

                )
                .setName(senderName)
                .setKey(senderId)
                .setImportant(true)
                .build()
    }

    private fun handleGroupNotification(
            remoteMessage: RemoteMessage
    ) = GlobalScope.launch {

        val person = createPerson(remoteMessage)
        val message = remoteMessage.notification?.body ?: ""
        val messageType = remoteMessage.data.getOrDefault("msg_type", "")
        val fullImagePath = remoteMessage.data.getOrDefault("image", "")
        val chatHeaderId: String = remoteMessage.data.getOrDefault("chat_header_id", "")

        val notificationMessage = NotificationCompat
                .MessagingStyle
                .Message(
                        message,
                        Date().time,
                        person
                )

        if (fullImagePath != null)
            notificationMessage.setData("image/*", Uri.parse(fullImagePath))

        val notificationBuilder =
                NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CHAT_ID)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setColor(Color.parseColor("#D72467"))
                        .setStyle(
                                NotificationCompat
                                        .MessagingStyle(person)
                                        .addMessage(notificationMessage)

                        )
                        .setAutoCancel(true)
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setOnlyAlertOnce(true)
                        .setVibrate(longArrayOf(100, 200, 300, 400, 500, 400))

        Log.d("ChatImage", fullImagePath)
        Log.d("GroupNotification", "notifi data  person: ${person.name} , msg: $message , type: $messageType ")
        var imageBitmap: Bitmap? = null
        if (fullImagePath.isNotBlank()) {
            imageBitmap = getBitmapFromURL(fullImagePath)
        }

        if (imageBitmap != null) {
            Log.d("ChatImage", "Icon Did set")
            notificationBuilder.setLargeIcon(imageBitmap)
        }

        notificationBuilder.setContentIntent(
                createPendingIntentForChat(
                        NotificationConstants.CLICK_ACTIONS.OPEN_GROUP_CHAT_PAGE,
                        remoteMessage.data
                )
        )

        val notification = notificationBuilder.build()
        notification.flags = Notification.FLAG_AUTO_CANCEL;
        NotificationHelper(context).createUrgentPriorityNotification(
                CHAT_NOTIF_REQUEST_CODE,
                notification

        )

//        var map = mapOf("chat_type" to "Group", "message_type" to messageType)
//        eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_NOTIFICATION_RECEIVED, map))
    }

    private fun showTextMessageReceivedNotification(
            text: String
    ) {

    }

    private fun showImageMessageReceivedNotification(
            thumbnailFullUrl: String,
            fullImageUrl: String
    ) {

    }

    private fun showDocumentMessageReceivedNotification(
            documentName: String
    ) {

    }

    private fun showVideoMessageReceivedNotification(
            videoName: String,
            thumbnailFullUrl: String
    ) {

    }

    companion object {
        const val CHAT_NOTIF_REQUEST_CODE = 67
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connectTimeout = 120 //connect timeout 2 minutes
            connection.readTimeout = 120    // read timeout 2 minutes
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}