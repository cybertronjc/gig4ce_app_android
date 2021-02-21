package com.gigforce.app.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.graphics.Color
import android.os.Build
import androidx.annotation.RequiresApi


object NotificationChannels {

    const val CHANNEL_URGENT_ID = "urgnt"
    private const val CHANNEL_URGENT_URGENT = "Urgent"

    const val CHANNEL_NORMAL_ID = "nrml"
    private const val CHANNEL_NORMAL_URGENT = "Normal"

    const val CHANNEL_CHAT_ID = "chat"
    private const val CHANNEL_CHAT = "Chat"

    val URGENT_NOTIFICATIONS
        @RequiresApi(Build.VERSION_CODES.O)
        get() =
            run {
                val channel = NotificationChannel(
                    CHANNEL_URGENT_ID,
                    CHANNEL_URGENT_URGENT,
                    NotificationManager.IMPORTANCE_HIGH
                )

                channel.enableLights(true)
                channel.lightColor = Color.RED
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400)
                channel
            }

    val CHAT_NOTIFICATIONS
        @RequiresApi(Build.VERSION_CODES.O)
        get() =
            run {
                val channel = NotificationChannel(
                    CHANNEL_CHAT_ID,
                    CHANNEL_CHAT,
                    NotificationManager.IMPORTANCE_HIGH
                )

                channel.enableLights(true)
                channel.lightColor = Color.RED
                channel.enableVibration(true)
                channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400)
                channel
            }

    val NORMAL_NOTIFICATIONS
        @RequiresApi(Build.VERSION_CODES.O)
        get() =
            run {
                val channel = NotificationChannel(
                    CHANNEL_NORMAL_ID,
                    CHANNEL_NORMAL_URGENT,
                    NotificationManager.IMPORTANCE_HIGH
                )

                channel.enableLights(true)
                channel.lightColor = Color.BLUE
                channel.enableVibration(true)
                channel
            }

}