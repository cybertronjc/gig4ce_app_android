package com.gigforce.core.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log
import android.widget.Toast


/**
 * Receiver for broadcasts sent by [LocationUpdatesService].
 */
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location =
            intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
        if (location != null) {
            Log.d("LocationUpdates", "loc: ${location.latitude} , ${location.longitude}")
            Toast.makeText(
                context, "Location",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}