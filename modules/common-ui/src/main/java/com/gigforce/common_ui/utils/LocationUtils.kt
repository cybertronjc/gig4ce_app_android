package com.gigforce.common_ui.utils

import android.content.Context
import android.location.Geocoder

object LocationUtils {

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