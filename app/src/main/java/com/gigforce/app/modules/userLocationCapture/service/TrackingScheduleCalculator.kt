package com.gigforce.app.modules.userLocationCapture.service

import com.gigforce.app.core.toLocalDateTime
import com.google.firebase.Timestamp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import java.time.LocalDateTime

object TrackingScheduleCalculator {
    private const val DEFAULT_TIME_DIFF_BETWEEN_ALARMS_IN_MINS = 15L //Mins
    private const val TIME_DIFF_BETWEEN_TWO_LOCATION_UPDATES = "time_diff"

    private val firebaseRemoteConfig: FirebaseRemoteConfig by lazy {
        FirebaseRemoteConfig.getInstance()
    }

    fun getPossibleAlarmTimesBetween(
            start: Timestamp,
            end: Timestamp
    ): List<LocalDateTime> {

        var startTime = start.toLocalDateTime()
        val endTime = end.toLocalDateTime()

        val diffBtwTwoAlarmsString = firebaseRemoteConfig.getString(TIME_DIFF_BETWEEN_TWO_LOCATION_UPDATES)
        val diff = if (diffBtwTwoAlarmsString.isNotBlank())
            diffBtwTwoAlarmsString.toLong()
        else
            DEFAULT_TIME_DIFF_BETWEEN_ALARMS_IN_MINS

        val alarms: MutableList<LocalDateTime> = mutableListOf()
        while (startTime.isBefore(endTime)) {
            alarms.add(startTime)
            startTime = startTime.plusMinutes(diff)
        }

        val currentTime = LocalDateTime.now()
        return alarms.filter {
            it.isAfter(currentTime)
        }
    }
}