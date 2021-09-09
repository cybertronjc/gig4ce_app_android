package com.gigforce.core.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.gigforce.core.R
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class LocationUpdates {
    private val locationAccessDialog: BsLocationAccess by lazy {
        BsLocationAccess()
    }

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Provides access to the Location Settings API.
     */
    private val mSettingsClient: SettingsClient? = null

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private val mLocationSettingsRequest: LocationSettingsRequest? = null

    /**
     * Callback for Location events.
     */
    private var mLocationCallback: LocationCallback? = null

    /**
     * Tracks the status of the location updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private var locationUpdateCallbacks: LocationUpdateCallbacks? = null

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest!!.interval = UPDATE_INTERVAL_IN_MILLISECONDS

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest!!.fastestInterval =
            FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }
    /**
     * Uses a [com.google.android.gms.location.LocationSettingsRequest.Builder] to build
     * a [com.google.android.gms.location.LocationSettingsRequest] that is used for checking
     * if a device has the needed location settings.
     */
    /**
     * Handles the Start Updates button and requests start of location updates. Does nothing if
     * updates have already been requested.
     */
    fun startUpdates(activity: AppCompatActivity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        createLocationRequest()
        createLocationCallbacks(activity)
        start(activity)
    }

    fun showLocationDialog(context: AppCompatActivity) {
        if (locationAccessDialog.dialog == null || locationAccessDialog.dialog?.isShowing == false) {
            locationAccessDialog.isCancelable = false
            locationAccessDialog.show(
                context.supportFragmentManager,
                BsLocationAccess::class.simpleName
            )


        }

    }

    private fun createLocationCallbacks(context: AppCompatActivity) {
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val lm: LocationManager =
                    context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                try {
                    if (!checkPermissions(context) || !lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        showLocationDialog(context)
                    }

                } catch (e: Exception) {

                    return
                }

                if (locationResult == null) {
                    Toast.makeText(context, context.getString(R.string.location_cant_be_found_core), Toast.LENGTH_SHORT).show()
                    return


                }
                for (location in locationResult.locations) {
                    // Update UI with location data
                    if (locationUpdateCallbacks != null) {
                        locationUpdateCallbacks!!.locationReceiver(location)
                    }
                    // ...
                }
                checkForLocationAccessDialog()
            }
        }
    }

    fun checkForLocationAccessDialog() {
        if (locationAccessDialog.dialog != null && locationAccessDialog.dialog?.isShowing == true) {
            locationAccessDialog.dismiss()
        }

    }

    @SuppressLint("MissingPermission")
    private fun getLastKnownLocation(context: Activity) {
        if (checkPermissions(context)) {
            checkForLocationAccessDialog()
            mFusedLocationClient!!.lastLocation
                .addOnSuccessListener(context) { location: Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        // Logic to handle location object
                        if (locationUpdateCallbacks != null) {
                            locationUpdateCallbacks!!.lastLocationReceiver(location)
                        }
                    }
                }
        }
    }


    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */
    @SuppressLint("MissingPermission")
    private fun startLocationUpdates(context: Activity) {
        // Begin by checking if the device has the necessary location settings.
        val builder = LocationSettingsRequest.Builder().addLocationRequest(
            mLocationRequest!!
        )
        val client = LocationServices.getSettingsClient(context)
        val task = client.checkLocationSettings(builder.build())
        task.addOnSuccessListener(context) { locationSettingsResponse: LocationSettingsResponse? ->
            // All location settings are satisfied. The client can initialize
            // location requests here.
            // ...
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return@addOnSuccessListener
            }
            mFusedLocationClient!!.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                null /* Looper */
            )
        }
        task.addOnFailureListener(context) { e: Exception? ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(context, REQUEST_CHECK_SETTINGS)
                } catch (sendEx: SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    /**
     * Removes location updates from the FusedLocationApi.
     */
    fun stopLocationUpdates(context: Activity?) {


        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        if (mFusedLocationClient != null && mLocationCallback != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(context!!) { }
        }
    }

    fun start(context: Activity) {

        // Within {@code onPause()}, we remove location updates. Here, we resume receiving
        // location updates if the user has requested them.
        if (checkPermissions(context)) {
            checkForLocationAccessDialog()
            getLastKnownLocation(context)
            startLocationUpdates(context)
        } else {
            showLocationDialog(context as AppCompatActivity)
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkPermissions(activity: Activity): Boolean {
        val permissionStateFineLocation = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val permissionStateCoarseLocation = ActivityCompat.checkSelfPermission(
            activity,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return permissionStateFineLocation == PackageManager.PERMISSION_GRANTED && permissionStateCoarseLocation == PackageManager.PERMISSION_GRANTED
    }

    fun setLocationUpdateCallbacks(locationUpdateCallbacks: LocationUpdateCallbacks?) {
        this.locationUpdateCallbacks = locationUpdateCallbacks
    }

    interface LocationUpdateCallbacks {
        fun locationReceiver(location: Location?)
        fun lastLocationReceiver(location: Location?)
    }

    companion object {
        /**
         * Code used in requesting runtime permissions.
         */
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34

        /**
         * Constant used in the location settings dialog.
         */
        const val REQUEST_CHECK_SETTINGS = 0x1
        private val TAG = LocationUpdates::class.java.simpleName

        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 2000

        /**
         * The fastest rate for active location updates. Exact. Updates will never be more frequent
         * than this value.
         */
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
}