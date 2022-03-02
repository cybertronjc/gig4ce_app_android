package com.gigforce.app.background.workers

import android.annotation.TargetApi
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.gigforce.app.eventbridge.EventBridgeRepo

class AttendanceWorker(var context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    var eventBridgeRepo = EventBridgeRepo(BuildConfig.EVENT_BRIDGE_URL)
    override suspend fun doWork(): Result {
//        setForeground(createForegroundInfo(context))
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        val notification = NotificationCompat.Builder(applicationContext, "1")
            //.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Gigforce - Attendance updation")
            .build()
        notificationManager?.notify(42, notification)
        var eventName = ""
        val mapData = HashMap<String, Any>()
        inputData.keyValueMap.forEach {
            if (it.key == "event_key") {
                eventName = it.value.toString()
            } else {
                mapData.put(it.key, it.value)
            }
        }
        eventBridgeRepo.setEventToEventBridge(eventName, mapData)

        return Result.success()
    }

    private fun createForegroundInfo(context: Context): ForegroundInfo {
        // Use a different id for each Notification.
        val notificationId = 1
        return ForegroundInfo(notificationId, createNotification(context))
    }

    /**
     * Create the notification and required channel (O+) for running work
     * in a foreground service.
     */
    private fun createNotification(context: Context): Notification {
        // This PendingIntent can be used to cancel the Worker.
        val intent = WorkManager.getInstance(context).createCancelPendingIntent(id)

        val builder = NotificationCompat.Builder(context, "1")
            .setContentTitle("Attendance")
            .setTicker("Attendance")
            .setSmallIcon(R.drawable.app_logo_background)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_delete, "Cancel", intent)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(context, "1", "Attendance").also {
                builder.setChannelId(it.id)
            }
        }
        return builder.build()
    }

    /**
     * Create the required notification channel for O+ devices.
     */
    @TargetApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(
        context: Context,
        channelId: String,
        name: String
    ): NotificationChannel {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as
                    NotificationManager
        return NotificationChannel(
            channelId, name, NotificationManager.IMPORTANCE_LOW
        ).also { channel ->
            notificationManager.createNotificationChannel(channel)
        }
    }


}