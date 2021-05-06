package com.gigforce.core.location;

import androidx.annotation.NonNull;

import com.google.android.gms.common.api.ResolvableApiException;

public interface GpsSettingsCheckCallback {


    /**
     * We don't have required Gps Settings
     * ex For High Accuracy Locations We Need Gps In High Accuracy Settings
     * <br/>
     * How To show "Turn On Gps Dialog" ?
     * <br/>
     * From Visit :<br/>
     * <code>status.startResolutionForResult(this , REQUEST_CHECK_SETTINGS);</code>
     * <br/>
     * From Fragment :<br/>
     * <code>
     * startIntentSenderForResult(status.getResolution().getIntentSender(), REQUEST_CHECK_SETTINGS, null, 0, 0, 0, null)
     * </code>
     */
    void requiredGpsSettingAreUnAvailable(@NonNull ResolvableApiException status);


    /**
     * Everything's Good
     */
    void requiredGpsSettingAreAvailable();


    /**
     * Gps Settings Are Unavailable redirect user to settings page to turn on location
     */
    void gpsSettingsNotAvailable();
}
