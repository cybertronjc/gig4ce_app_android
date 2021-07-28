package com.gigforce.app.notification

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.notification.NotificationChannels.CHAT_NOTIFICATIONS
import com.gigforce.app.notification.NotificationChannels.URGENT_NOTIFICATIONS
import com.gigforce.app.services.SyncUnSyncedDataService
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.user_tracking.TrackingConstants
import com.gigforce.user_tracking.service.TrackingService
import com.gigforce.user_tracking.workers.TrackingWorker
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
        val silentPushPurpose =
            data.getOrDefault(NotificationConstants.GlobalKeys.SILENT_PURPOSE, "-")
        Log.d("NotificationHelper", "silent push received ,purpose : $silentPushPurpose")

        when (silentPushPurpose) {
            NotificationConstants.GlobalKeys.TASK_UNSYNCED_DATA -> startSyncUnSyncedDataWorker(
                context
            )
            NotificationConstants.GlobalKeys.TASK_SYNC_GEOFENCES -> startSyncGeoFenceWorker(
                context
            )
            NotificationConstants.GlobalKeys.TASK_SYNC_CURRENT_LOCATION_FOR_GIG -> startSyncCurrentLocationForGigWorker(
                context,
                data
            )
            else -> {
                //No Match
            }
        }
    }

    private fun startSyncCurrentLocationForGigWorker(
        context: Context,
        data: Map<String, String>
    ) {
        try {
            val intent = Intent(context, TrackingService::class.java).apply {
                action = TrackingConstants.ACTION_START_OR_RESUME_SERVICE
                this.putExtra(
                    TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID)
                )
                this.putExtra(
                    TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME)
                )
                this.putExtra(
                    TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME)
                )
            }
            context.startService(intent)
        } catch (e: IllegalStateException) {
            // App is probably in background hence not able to start Service
            // Using Work Manager

            val workerData = Data.Builder()
                .putString(
                    TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID)
                ).putString(
                    TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME)
                ).putString(
                    TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME,
                    data.get(TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME)
                ).build()

            val request =
                OneTimeWorkRequestBuilder<TrackingWorker>().setInputData(workerData).build()
            WorkManager.getInstance(context).enqueue(request)
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e(
                LOG_TAG,
                "while starting TrackingService",
                e
            )
        }
    }

    private fun startSyncGeoFenceWorker(
        context: Context
    ) {
    }

    private fun startSyncUnSyncedDataWorker(
        context: Context
    ) {

        val intent = Intent(context, SyncUnSyncedDataService::class.java).apply {
            action = TrackingConstants.ACTION_START_OR_RESUME_SERVICE
        }
        context.startService(intent)
    }

    companion object {

        const val LOG_TAG = "NotificationHelper"

        fun isSilentPush(
            data: Map<String, String?>?
        ): Boolean {
            val dataMap = data ?: return false
            return dataMap.containsKey(NotificationConstants.GlobalKeys.IS_SILENT_PUSH) &&
                    "true" == dataMap.get(NotificationConstants.GlobalKeys.IS_SILENT_PUSH)
        }
    }

}