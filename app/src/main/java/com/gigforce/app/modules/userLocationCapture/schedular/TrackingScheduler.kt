package com.gigforce.app.modules.userLocationCapture.schedular

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gigforce.app.modules.gigPage2.models.Gig
import com.gigforce.app.modules.gigPage2.repositories.GigsRepository
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.userLocationCapture.TrackingConstants
import com.gigforce.app.modules.userLocationCapture.service.TrackingScheduleCalculator
import com.gigforce.app.modules.userLocationCapture.service.TrackingService
import com.gigforce.core.crashlytics.CrashlyticsLogger
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TrackingScheduler constructor(
        private val context: Context,
        private val gigsRepository: GigsRepository = GigsRepository(),
        private val profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) {

    private val applicationContext: Context = context.applicationContext

    fun checkForTodaysGigsAndScheduleTrackers() = GlobalScope.launch {
        scheduleTrackers()
    }

    fun scheduleTrackerForGig(
            gig: Gig
    ) = GlobalScope.launch {

        val profile = try {
            profileFirebaseRepository.getProfileDataIfExist()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

        val possibleAlarmsBtw = TrackingScheduleCalculator.getPossibleAlarmTimesBetween(
                start = gig.startDateTime,
                end = gig.endDateTime
        )
        Log.d(TAG, "Gig Start time : ${gig.startDateTime}, end time : ${gig.endDateTime}")
        Log.d(TAG, "Possible Alarms : ${possibleAlarmsBtw}")

        scheduleAlarmsBetween(
                alarmTimes = possibleAlarmsBtw,
                gigId = gig.gigId,
                userName = profile?.name
        )
    }

    private suspend fun scheduleTrackers() {
        try {

            val gigs = gigsRepository.getOngoingAndUpcomingGigsFor(LocalDate.now())
            val profile = profileFirebaseRepository.getProfileDataIfExist()

            for (gig in gigs) {

                val possibleAlarmsBtw = TrackingScheduleCalculator.getPossibleAlarmTimesBetween(
                        start = gig.startDateTime,
                        end = gig.endDateTime
                )
                scheduleAlarmsBetween(
                        alarmTimes = possibleAlarmsBtw,
                        gigId = gig.gigId,
                        userName = profile?.name
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CrashlyticsLogger.e(
                    TAG,
                    "Scheduling Alarms",
                    e
            )
        }
    }


    private fun scheduleAlarmsBetween(
            alarmTimes: List<LocalDateTime>,
            gigId: String,
            userName: String?

    ) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //Preparing Intent Extras
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingConstants.ACTION_START_OR_RESUME_SERVICE
            this.putExtra(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID, gigId)
            this.putExtra(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME, userName)
        }

        var alarmsId = 0
        for (alarmTime in alarmTimes) {
            val pIntent = PendingIntent.getService(
                    applicationContext,
                    alarmsId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_ONE_SHOT
            )
            alarmsId++

            val alarmToTriggerAt = alarmTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    alarmToTriggerAt,
                    pIntent
            )
        }
    }

    companion object {
        private const val TAG = "TrackingScheduler"
    }
}