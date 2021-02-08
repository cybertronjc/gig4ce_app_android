package com.gigforce.app.notification

import android.app.PendingIntent
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.graphics.drawable.IconCompat
import androidx.navigation.NavDeepLinkBuilder
import com.gigforce.app.R
import com.gigforce.app.core.toBundle
import com.gigforce.core.extensions.getDownloadUrlOrReturnNull
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class ChatNotificationHandler constructor(
        private val context: Context
) {

    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    fun handleChatNotification(
            remoteMessage: RemoteMessage
    ) = GlobalScope.launch {

        val isGroupMessage = remoteMessage.data.getOrDefault("is_group_conversation", "false") == "true"
        if (isGroupMessage) {
            handleGroupNotification(remoteMessage)
        } else {

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

            val notificationBuilder = NotificationCompat.Builder(context, NotificationChannels.CHANNEL_CHAT_ID)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setColor(Color.parseColor("#D72467"))
                    .setStyle(
                            NotificationCompat
                                    .MessagingStyle(person)
                                    .addMessage(notificationMessage)

                    )
                    .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                    .setOnlyAlertOnce(true)

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
                    createPendingIntentForChat(remoteMessage.data)
            )

            val notification = notificationBuilder.build()
            NotificationHelper(context).createUrgentPriorityNotification(
                    CHAT_NOTIF_REQUEST_CODE,
                    notification

            )
        }
    }

    private fun createPendingIntentForChat(
            data: Map<String, String>
    ): PendingIntent {
        val dataBundle = data.toBundle()
        return NavDeepLinkBuilder(context)
                .setGraph(R.navigation.nav_graph)
                .setDestination(R.id.chatPageFragment)
                .setArguments(dataBundle)
                .createPendingIntent()
    }

    private suspend fun createPerson(remoteMessage: RemoteMessage): Person {
        val senderName = remoteMessage.notification?.title ?: ""
        val senderId = remoteMessage.data.getOrDefault("sender_id", "")

        val senderProfilePicturePath = remoteMessage.data.getOrDefault("sender_profile", "")
        var senderProfilePicture: Bitmap? = null
        if (senderProfilePicturePath != "") {
            val senderProfilePictureUri = firebaseStorage.reference.child(senderProfilePicturePath).getDownloadUrlOrReturnNull()

            if (senderProfilePictureUri != null)
                senderProfilePicture = getBitmapFromURL(senderProfilePictureUri.toString())
        }

        return Person.Builder()
                .setIcon(
                        if (senderProfilePicture != null)
                            IconCompat.createWithBitmap(senderProfilePicture)
                        else
                            IconCompat.createWithResource(context, R.drawable.ic_user)

                )
                .setName(senderName)
                .setKey(senderId)
                .setImportant(true)
                .build()
    }

    private fun handleGroupNotification(remoteMessage: RemoteMessage) {

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

        const val CHAT_NOTIF_CHANNEL_ID = "chat_notifications"
        const val CHAT_NOTIF_CHANNEL_NAME = "Chat Notifications"

        const val CHAT_NOTIF_REQUEST_CODE = 67
    }

    private fun getBitmapFromURL(src: String): Bitmap? {
        return try {
            val url = URL(src)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input: InputStream = connection.getInputStream()
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}