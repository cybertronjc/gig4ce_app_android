package com.gigforce.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.DeepLinkActivity
import com.gigforce.app.R
import com.gigforce.app.notification.NotificationChannels.CHAT_NOTIFICATIONS
import com.gigforce.app.notification.NotificationChannels.URGENT_NOTIFICATIONS
import kotlin.random.Random


class NotificationHelper(private val mContext: Context) {

    /**
     * Create and push the notification
     */
    fun createUrgentPriorityNotification(
            title: String,
            message: String,
            pendingIntent: PendingIntent? = null
    ) {

        val finalPendingIntent = if (pendingIntent == null) {
            /**Creates an explicit intent for an Activity in your app */
            val resultIntent = Intent(mContext, DeepLinkActivity::class.java)
            resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val resultPendingIntent = PendingIntent.getActivity(
                    mContext,
                    0 /* Request code */, resultIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
            )
            resultPendingIntent
        } else {
            pendingIntent
        }

        val mBuilder = NotificationCompat.Builder(
                mContext,
                NotificationChannels.CHANNEL_URGENT_ID
        )

        mBuilder.setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(mContext.resources, R.color.colorPrimary, null))
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(finalPendingIntent)

        val mNotificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(URGENT_NOTIFICATIONS)
        }

        val reqCode = Random.nextInt(0, 100)
        mNotificationManager.notify(reqCode /* Request Code */, mBuilder.build())
    }

    fun createUrgentPriorityNotification(
            requestCode: Int,
            notification: Notification
    ) {



        val mNotificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(CHAT_NOTIFICATIONS)
        }

        mNotificationManager.notify(requestCode /* Request Code */, notification)
    }


    /**
     * Create and push the notification
     */
    fun createNormalPriorityNotification(
            title: String,
            message: String
    ) {
        /**Creates an explicit intent for an Activity in your app */
        val resultIntent = Intent(mContext, DeepLinkActivity::class.java)
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val resultPendingIntent = PendingIntent.getActivity(
                mContext,
                0 /* Request code */, resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT
        )

        val mBuilder = NotificationCompat.Builder(
                mContext,
                NotificationChannels.CHANNEL_URGENT_ID
        )

        mBuilder.setSmallIcon(R.drawable.ic_notification_icon)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(ResourcesCompat.getColor(mContext.resources, R.color.colorPrimary, null))
                .setAutoCancel(true)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setContentIntent(resultPendingIntent)

        val mNotificationManager =
                mContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager.createNotificationChannel(URGENT_NOTIFICATIONS)
        }

        val reqCode = Random.nextInt(0, 100)
        mNotificationManager.notify(reqCode /* Request Code */, mBuilder.build())
    }

}