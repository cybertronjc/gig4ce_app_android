package com.gigforce.app.utils

import android.content.Context
import android.location.Geocoder
import android.location.Location

object LocationUtils {

    private const val TWO_MINUTES = 1000 * 60 * 2


    fun isBetterLocation(location: Location, currentBestLocation: Location?): Boolean {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true
        }

        if (currentBestLocation.latitude == location.latitude
                && currentBestLocation.longitude == location.longitude
        ) return false

        val timeDelta = location.time - currentBestLocation.time
        val isSignificantlyNewer = timeDelta > TWO_MINUTES
        val isSignificantlyOlder = timeDelta < -TWO_MINUTES
        val isNewer = timeDelta > 0

        if (isSignificantlyNewer) {
            return true
        } else if (isSignificantlyOlder) {
            return false
        }

        // Check whether the new location fix is more or less accurate
        val accuracyDelta = (location.accuracy - currentBestLocation.accuracy).toInt()
        val isLessAccurate = accuracyDelta > 0
        val isMoreAccurate = accuracyDelta < 0
        val isSignificantlyLessAccurate = accuracyDelta > 200

        val isFromSameProvider = isSameProvider(
                location.provider,
                currentBestLocation.provider
        )

        if (isMoreAccurate) {
            return true
        } else if (isNewer && !isLessAccurate) {
            return true
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true
        }
        return false
    }

    /**
     * Checks whether two providers are the same
     */
    private fun isSameProvider(provider1: String?, provider2: String?): Boolean {
        return if (provider1 == null) {
            provider2 == null
        } else provider1 == provider2
    }

    fun getPhysicalAddressFromLocation(
            context: Context,
            latitude: Double,
            longitude: Double
    ): String {

        return try {
            val locationResults = Geocoder(context)
                    .getFromLocation(latitude, longitude, 1)

            if (locationResults.isEmpty()) {
                ""
            } else {
                locationResults.first().getAddressLine(0)
            }
        } catch (e: Exception) {
            return ""
        }
    }
}