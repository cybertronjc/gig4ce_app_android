package com.gigforce.app.modules.userLocationCapture.service

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
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.gigforce.app.MainActivity
import com.gigforce.app.R
import com.gigforce.app.modules.userLocationCapture.TrackingConstants
import com.gigforce.app.modules.userLocationCapture.TrackingConstants.ACTION_PAUSE_SERVICE
import com.gigforce.app.modules.userLocationCapture.TrackingConstants.ACTION_STOP_SERVICE
import com.gigforce.app.modules.userLocationCapture.TrackingConstants.NOTIFICATION_CHANNEL_ID
import com.gigforce.app.modules.userLocationCapture.TrackingConstants.NOTIFICATION_ID
import com.gigforce.app.modules.userLocationCapture.TrackingUtility
import com.gigforce.app.modules.userLocationCapture.repository.UserLocationRepository
import com.gigforce.app.utils.LocationUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
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

    //Data from UI
    private var gigId: String? = null
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
            gigId = it.getStringExtra(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID)
            userName = it.getStringExtra(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME)

            when (it.action) {

                TrackingConstants.ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Log.d(TAG, "Resuming service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Log.d(TAG, "Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Log.d(TAG, "Stopped service")
                }
                else -> {

                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
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
            submitLocationToDB(location, fullAddressFromGps)
        } else {

            if (LocationUtils.isBetterLocation(location, currentBestLocation!!)) {
                Log.d(TAG, "Received a Better location, total location updates received so far $locationUpdatesReceivedSoFar")
                currentBestLocation = location
                submitLocationToDB(location, fullAddressFromGps)
            }
        }

        if (locationUpdatesReceivedSoFar == MAX_LOCATION_UPDATES_IN_ONE_SESSION) {
            //Stopping the Service
            Log.d(TAG, "Stopping Service....")
            stopForeground(true)
            stopSelf()
        }
    }

    private fun submitLocationToDB(
            location: Location,
            fullAddressFromGps: String
    ) = GlobalScope.launch {

        userLocationRepository.updateUserLocation(
                location = LatLng(location.latitude, location.longitude),
                accuracy = location.accuracy,
                gigId = gigId,
                userName = userName,
                couldBeAFakeLocation = location.isFromMockProvider,
                fullAddressFromGps = fullAddressFromGps
        )
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
                .setContentTitle("Checking Location...")
                .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).also {
                it.action = TrackingConstants.ACTION_SHOW_TRACKING_FRAGMENT
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
    }
}