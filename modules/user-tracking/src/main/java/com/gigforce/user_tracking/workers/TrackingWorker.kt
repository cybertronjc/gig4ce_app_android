package com.gigforce.user_tracking.workers

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.user_tracking.TrackingConstants
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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.resumeWithException

class TrackingWorker(
    private val appContext: Context,
    private val params: WorkerParameters
) : CoroutineWorker(
    appContext = appContext,
    params = params
) {

    //Data
    private var currentBestLocation: Location? = null
    private var locationUpdatesReceivedSoFar = 0
    private lateinit var gigId: String
    private lateinit var userName: String
    private lateinit var fullCompanyName: String

    private val fusedLocationProviderClient: FusedLocationProviderClient by lazy {
        FusedLocationProviderClient(appContext)
    }

    private val userLocationRepository: UserLocationRepository by lazy {
        UserLocationRepository()
    }

    override suspend fun doWork(): Result {
        return try {
            checkForLocation()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun checkForLocation() = withTimeout(MAX_TIME_FOR_WHICH_SERVICE_CAN_RUN) {
        gigId = params.inputData.getString(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID)!!
        userName = params.inputData.getString(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME)!!
        fullCompanyName =
            params.inputData.getString(TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME)
                ?: "Gigforce"

        try {
            updateLocationTracking()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    @SuppressLint("MissingPermission")
    private suspend fun updateLocationTracking() = suspendCancellableCoroutine<Any?> { cont ->
        cont.invokeOnCancellation {
            releaseGps()
        }

        if (TrackingUtility.hasLocationPermissions(appContext)) {
            Log.d(TAG,"Location permission available")
            val request = LocationRequest().apply {
                interval = TrackingConstants.LOCATION_UPDATE_INTERVAL
                fastestInterval = TrackingConstants.FASTEST_LOCATION_INTERVAL
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }
            fusedLocationProviderClient.requestLocationUpdates(
                request,
                locationCallback,
                Looper.getMainLooper()
            ).addOnSuccessListener {
                Log.d(TAG,"Location updates started")
            }.addOnFailureListener {
                Log.e(TAG,"Location update not able to start")
            }.addOnCanceledListener {
                Log.e(TAG,"Location updates cancelled")
            }
        } else {
            Log.e(TAG,"Location permission not available")
            cont.resumeWithException(
                IllegalStateException(
                    "Location permission not granted"
                )
            )
        }
    }

    private fun releaseGps() {
        try {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            super.onLocationResult(result)

            result?.locations?.let { locations ->
                for (location in locations) {
                    addPathPoint(location)
                    Log.d(TAG, "NEW LOCATION: ${location.latitude}, ${location.longitude}")
                }
            }
        }
    }

    private fun addPathPoint(
        location: Location?
    ) = location?.let {
        compareLocationsAndSubmit(it)
    }

    private fun compareLocationsAndSubmit(
        location: Location
    ) {
        locationUpdatesReceivedSoFar++

        val fullAddressFromGps = LocationUtils.getPhysicalAddressFromLocation(
            appContext,
            location.latitude,
            location.longitude
        )
        if (currentBestLocation == null) {
            currentBestLocation = location
        } else {

            if (LocationUtils.isBetterLocation(location, currentBestLocation!!)) {
                Log.d(
                    TAG,
                    "Received a Better location, total location updates received so far $locationUpdatesReceivedSoFar"
                )
                currentBestLocation = location
            }
        }

        submitLocationToDB(currentBestLocation!!, fullAddressFromGps)
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

            e.printStackTrace()
            CrashlyticsLogger.e(
                TAG,
                "unable to sync user location",
                e
            )
        }
    }

    companion object {
        const val TAG = "TrackingWorker"
        const val MAX_LOCATION_UPDATES_IN_ONE_SESSION = 5
        const val MAX_TIME_FOR_WHICH_SERVICE_CAN_RUN = 30L * 1000
    }
}