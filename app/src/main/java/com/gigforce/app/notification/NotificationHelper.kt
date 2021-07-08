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
import androidx.work.BackoffPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.background.workers.SyncUnSyncedDataToDatabaseWorker
import com.gigforce.app.notification.NotificationChannels.CHAT_NOTIFICATIONS
import com.gigforce.app.notification.NotificationChannels.URGENT_NOTIFICATIONS
import java.util.concurrent.TimeUnit
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
            val resultIntent = Intent(mContext, MainActivity::class.java)
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
        val resultIntent = Intent(mContext, MainActivity::class.java)
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

    fun handleSilentPush(
            context: Context,
            data: Map<String, String>
    ) {

        when (data.getOrDefault(NotificationConstants.GlobalKeys.SILENT_PURPOSE, "-")) {
            NotificationConstants.GlobalKeys.TASK_UNSYNCED_DATA -> startSyncUnSyncedDataWorker(context)
            NotificationConstants.GlobalKeys.TASK_SYNC_GEOFENCES -> startSyncGeoFenceWorker(context)
            NotificationConstants.GlobalKeys.TASK_SYNC_CURRENT_LOCATION_FOR_GIG -> startSyncCurrentLocationForGigWorker(context)
            else -> {
                //No Match
            }
        }
    }

    private fun startSyncCurrentLocationForGigWorker(
            context: Context
    ) {

    }

    private fun startSyncGeoFenceWorker(
            context: Context
    ) {
        TODO("Not yet implemented")
    }

    private fun startSyncUnSyncedDataWorker(
            context: Context
    ) {
        val workRequest = OneTimeWorkRequestBuilder<SyncUnSyncedDataToDatabaseWorker>()
                .setBackoffCriteria(BackoffPolicy.LINEAR, 20 , TimeUnit.MINUTES)
                .build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    companion object {

        fun isSilentPush(
                data: Map<String, String?>?
        ): Boolean {
            val dataMap = data ?: return false
            return dataMap.containsKey(NotificationConstants.GlobalKeys.IS_SILENT_PUSH) && "true" == dataMap.get(NotificationConstants.GlobalKeys.IS_SILENT_PUSH)
        }
    }

}