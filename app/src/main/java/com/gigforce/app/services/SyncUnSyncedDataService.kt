package com.gigforce.app.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.user_tracking.R
import com.gigforce.user_tracking.TrackingConstants
import com.gigforce.user_tracking.TrackingConstants.NOTIFICATION_CHANNEL_ID
import com.gigforce.user_tracking.TrackingConstants.NOTIFICATION_ID
import com.google.firebase.firestore.FirebaseFirestore

class SyncUnSyncedDataService : LifecycleService() {

    private var isFirstRun = true

    private val firebaseFirestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val handler = Handler()
    override fun onCreate() {
        super.onCreate()

        isTracking.observe(this, {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {

            when (it.action) {

                TrackingConstants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        setTimerOfMax30SecsToCloseService()
                        isFirstRun = false
                    } else {
                        Log.d(TAG, "Resuming service...")
                    }
                }
                TrackingConstants.ACTION_PAUSE_SERVICE -> {
                    Log.d(TAG, "Paused service")
                }
                TrackingConstants.ACTION_STOP_SERVICE -> {
                    Log.d(TAG, "Stopped service")
                }
                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setTimerOfMax30SecsToCloseService() {
        handler.postDelayed(serviceMaxTimerRunnable, MAX_TIME_FOR_WHICH_SERVICE_CAN_RUN)
    }

    private fun updateLocationTracking(isTracking: Boolean) {

        firebaseFirestore.waitForPendingWrites().addOnSuccessListener {

            Log.d(TAG, "unsynced data synced")
            stopService()
        }.addOnFailureListener {

            CrashlyticsLogger.e(TAG, "while syncing pending data", it)
            stopService()
        }.addOnCanceledListener {

            stopService()
        }
    }

    private fun stopService() {
        Log.d(TAG, "Stopping Service....")
        handler.removeCallbacks(serviceMaxTimerRunnable)
        stopForeground(true)
        stopSelf()
    }

    private val serviceMaxTimerRunnable = Runnable {
        Log.d(TAG, "Stopping Service from timer....")
        stopForeground(true)
        stopSelf()
    }

    private fun startForegroundService() {
        isTracking.postValue(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setColor(
                ResourcesCompat.getColor(
                applicationContext.resources,
                R.color.notification_icon_color,
                null
            ))
            .setContentTitle("Syncing data to server...")

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            TrackingConstants.NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        val isTracking = MutableLiveData<Boolean>()

        const val TAG = "SyncUnSyncedDataService"
        const val MAX_TIME_FOR_WHICH_SERVICE_CAN_RUN = 50L * 1000
    }
}