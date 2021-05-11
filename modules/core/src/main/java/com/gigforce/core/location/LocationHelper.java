package com.gigforce.core.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;


/**
 * @Author - Himanshu
 */
@SuppressLint({"MissingPermission", "LogNotTimber"})
public class LocationHelper {

    private static final String TAG = LocationHelper.class.getSimpleName();

    /**
     * The desired interval for jobLocation updates. Inexact. Updates may be more or less frequent.
     */
    private long updateIntervalInMilliseconds = 10000;

    /**
     * The fastest rate for active jobLocation updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private long fastestUpdateIntervalInMilliseconds = updateIntervalInMilliseconds / 2;

    /**
     * Provides access to the Fused SuggestedLocation Provider API.
     */
    private FusedLocationProviderClient mFusedLocationClient;

    /**
     * Provides access to the SuggestedLocation Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    /**
     * Stores the types of jobLocation services the client is interested in using. Used for checking
     * settings to determine if the device has optimal jobLocation settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Callback for SuggestedLocation events.
     */
    private LocationCallback mLocationCallback;


    /**
     * Tracks the status of the jobLocation updates request. Value changes when the user presses the
     * Start Updates and Stop Updates buttons.
     */
    private Boolean mRequestingLocationUpdates = false;


    private int requiredGpsPriority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY;


    public LocationHelper(Context mContext) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
        mSettingsClient = LocationServices.getSettingsClient(mContext);
    }

    /**
     * Sets required gps priority
     * <p>
     * Gps Priority can be
     * <ul>
     * <li>LocationRequest.PRIORITY_HIGH_ACCURACY</li>
     * <li>LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY</li>
     * <li>LocationRequest.PRIORITY_NO_POWER</li>
     * <li>LocationRequest.PRIORITY_LOW_POWER</li>
     * </ul>
     * <p>
     * default is LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
     *
     * @param requiredGpsPriority gps priority
     */
    public void setRequiredGpsPriority(int requiredGpsPriority) {
        this.requiredGpsPriority = requiredGpsPriority;
    }

    /**
     * Sets Update Interval also sets fastestUpdateIntervalInMilliseconds to half of updateIntervalInMilliseconds
     * default is 10 seconds
     *
     * @param updateIntervalInMilliseconds update Interval
     */
    public void setUpdateInterval(long updateIntervalInMilliseconds) {
        this.updateIntervalInMilliseconds = updateIntervalInMilliseconds;
        this.fastestUpdateIntervalInMilliseconds = updateIntervalInMilliseconds / 2;
    }

    /**
     * Sets fastest Update Interval
     * default is 5 seconds
     *
     * @param fastestUpdateIntervalInMilliseconds fastest update Interval
     */
    public void setFastestUpdateIntervalInMilliseconds(long fastestUpdateIntervalInMilliseconds) {
        this.fastestUpdateIntervalInMilliseconds = fastestUpdateIntervalInMilliseconds;
    }


    public void init() {
        // Kick off the process of building the LocationCallback, LocationRequest, and
        // LocationSettingsRequest objects.
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    /**
     * Set Callback for getting location updates
     *
     * @param locationCallback
     */
    public void setLocationCallback(LocationCallback locationCallback) {
        this.mLocationCallback = locationCallback;
    }


    /**
     * Sets up the jobLocation request. Android has two jobLocation request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current jobLocation. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused SuggestedLocation Provider API returns jobLocation updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time jobLocation
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no jobLocation sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting jobLocation at a faster interval.
        mLocationRequest.setInterval(updateIntervalInMilliseconds);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(fastestUpdateIntervalInMilliseconds);

        // Set Gps Priority
        mLocationRequest.setPriority(requiredGpsPriority);
    }


    /**
     * Uses a {@link LocationSettingsRequest.Builder} to build
     * a {@link LocationSettingsRequest} that is used for checking
     * if a device has the needed jobLocation settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        builder.setAlwaysShow(true);
        mLocationSettingsRequest = builder.build();
    }


    /**
     * Tells if current jobLocation is being requested or not
     */
    public boolean isRequestingForLocation() {
        return mRequestingLocationUpdates;
    }

    /**
     * Requests jobLocation updates from the FusedLocationApi. Note: we don't call this unless jobLocation
     * runtime permission has been granted.
     */

    public void checkForGpsSettings(GpsSettingsCheckCallback callback) {

        if (mLocationSettingsRequest == null) {
            throw new IllegalStateException("must call init() before check for gps settings");
        }

        if (mSettingsClient == null) {
            return;
        }
        // Begin by checking if the device has the necessary jobLocation settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(locationSettingsResponse -> callback.requiredGpsSettingAreAvailable())
                .addOnFailureListener(e -> {

                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            Log.i(TAG, "SuggestedLocation settings are not satisfied. notifying back to the requesting object ");

                            ResolvableApiException rae = (ResolvableApiException) e;
                            callback.requiredGpsSettingAreUnAvailable(rae);

                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            Log.i(TAG, "Turn On SuggestedLocation From Settings. ");

                            callback.gpsSettingsNotAvailable();
                            break;
                    }

                });
    }

    /**
     * Starts location updates from the FusedLocationApi.
     * <p>
     * Consider Calling {@link #stopLocationUpdates()} when you don't want location updates it helps in saving battery
     * </p>
     */

    public void startLocationUpdates() {

        if (mLocationRequest == null) {
            throw new IllegalStateException("must call init() before requesting location updates");
        }

        if (mLocationCallback == null) {
            throw new IllegalStateException("no callback provided for delivering location updates,use setLocationCallback() for setting callback");
        }

        if (mRequestingLocationUpdates) {
            Log.d(TAG, "startLocationUpdates: already requesting location updates, no-op.");
            return;
        }

        if (mFusedLocationClient == null) {
            return;
        }

        Log.d(TAG, "startLocationUpdates: starting updates.");
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
                .addOnSuccessListener(task -> mRequestingLocationUpdates = true);

    }


    /**
     * Removes location updates from the FusedLocationApi.
     * <p>
     * It is a good practice to remove location requests when the activity is in a paused or
     * stopped state. Doing so helps battery performance and is especially
     * recommended in applications that request frequent jobLocation updates.
     * </p>
     */
    public void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        if (mFusedLocationClient == null) {
            return;
        }

        if (mLocationCallback == null) {
            return;
        }

        Log.d(TAG, "stopLocationUpdates: stopping location updates.");
        mFusedLocationClient
                .removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(task -> mRequestingLocationUpdates = false);
    }

}
