package com.gigforce.common_ui.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.location.Location
import android.util.Log


/**
 * Receiver for broadcasts sent by [LocationUpdatesService].
 */
class MyReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val location =
            intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
        if (location != null) {
            Log.d("LocationUpdates", "loc: ${location.latitude} , ${location.longitude}")
            context.sendBroadcast(Intent(LocationUpdatesService.ACTION_BROADCAST))
        }
    }
}