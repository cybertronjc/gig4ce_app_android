package com.gigforce.user_tracking.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.Looper
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
import com.gigforce.user_tracking.TrackingUtility
import com.gigforce.user_tracking.models.LatLng
import com.gigforce.user_tracking.repository.UserLocationRepository
import com.gigforce.user_tracking.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TrackingService : LifecycleService() {

    private var currentBestLocation: Location? = null
    private var locationUpdatesReceivedSoFar = 0
    private var isFirstRun = true
    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(this)
    }
    private val userLocationRepository: UserLocationRepository by lazy {
        UserLocationRepository()
    }

    private fun postInitialValues() {
        isTracking.postValue(false)
    }

    private val handler = Handler()

    //Data from UI
    private lateinit var gigId: String
    private lateinit var fullCompanyName: String
    private var userName: String? = null

    override fun onCreate() {
        super.onCreate()
        postInitialValues()

        isTracking.observe(this, {
            updateLocationTracking(it)
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            gigId = it.getStringExtra(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID)!!
            userName = it.getStringExtra(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME)
            fullCompanyName = it.getStringExtra(TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME) ?: "Gigforce"

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

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtility.hasLocationPermissions(this)) {
                val request = LocationRequest().apply {
                    interval = TrackingConstants.LOCATION_UPDATE_INTERVAL
                    fastestInterval = TrackingConstants.FASTEST_LOCATION_INTERVAL
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                        request,
                        locationCallback,
                        Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)
            if (isTracking.value!!) {
                result?.locations?.let { locations ->
                    for (location in locations) {
                        addPathPoint(location)
                        Log.d(TAG, "NEW LOCATION: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }

    private val serviceMaxTimerRunnable = Runnable {
        Log.d(TAG, "Stopping Service from timer....")
        stopForeground(true)
        stopSelf()
    }

    private fun addPathPoint(location: Location?) = location?.let {
        compareLocationsAndSubmit(it)
    }


    private fun compareLocationsAndSubmit(
            location: Location
    ) {
        locationUpdatesReceivedSoFar++

        if (locationUpdatesReceivedSoFar == MAX_LOCATION_UPDATES_IN_ONE_SESSION) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            Log.d(TAG, "Stopping Location updates, received $locationUpdatesReceivedSoFar locations so far")
        }

        val fullAddressFromGps = LocationUtils.getPhysicalAddressFromLocation(
                this,
                location.latitude,
                location.longitude
        )
        if (currentBestLocation == null) {
            currentBestLocation = location
        } else {

            if (LocationUtils.isBetterLocation(location, currentBestLocation!!)) {
                Log.d(TAG, "Received a Better location, total location updates received so far $locationUpdatesReceivedSoFar")
                currentBestLocation = location
            }
        }

        if (locationUpdatesReceivedSoFar == MAX_LOCATION_UPDATES_IN_ONE_SESSION) {
            submitLocationToDB(currentBestLocation!!, fullAddressFromGps)

            Log.d(TAG, "Stopping Service....")
            handler.removeCallbacks(serviceMaxTimerRunnable)
            stopForeground(true)
            stopSelf()
        }
    }

    private fun submitLocationToDB(
            location: Location,
            fullAddressFromGps: String
    ) = GlobalScope.launch {

        try {
            userLocationRepository.updateUserLocation(
                    location = LatLng(location.latitude, location.longitude),
                    accuracy = location.accuracy,
                    gigId = gigId,
                    couldBeAFakeLocation = location.isFromMockProvider,
                    fullAddressFromGps = fullAddressFromGps
            )
        } catch (e: Exception) {

            CrashlyticsLogger.e(
                TAG,
                "unable to sync user location",
                e
            )
        }
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
                    )
                   )
                .setContentTitle("Fetching location for $fullCompanyName Gig")
                .setContentText("Tap for details")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this,  Class.forName("com.gigforce.app.MainActivity")).also {
                it.putExtra("gig_id", gigId)
                it.putExtra("is_deeplink", "true")
                it.putExtra("click_action", "com.gigforce.app.gig.open_gig_page_2")
            },
            FLAG_UPDATE_CURRENT
    )

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

        const val TAG = "TrackingService"
        const val MAX_LOCATION_UPDATES_IN_ONE_SESSION = 5
        const val MAX_TIME_FOR_WHICH_SERVICE_CAN_RUN = 30L * 1000
    }
}