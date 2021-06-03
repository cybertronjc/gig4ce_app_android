package com.gigforce.user_tracking.schedular

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.user_tracking.TrackingConstants
import com.gigforce.user_tracking.repository.TrackingUserProfileRepository
import com.gigforce.user_tracking.service.TrackingScheduleCalculator
import com.gigforce.user_tracking.service.TrackingService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class TrackingScheduler constructor(
    private val context: Context,
    private val profileFirebaseRepository: TrackingUserProfileRepository = TrackingUserProfileRepository()
) {

    private val applicationContext: Context = context.applicationContext

    private val firebaseUser : FirebaseUser? get() {
       return FirebaseAuth.getInstance().currentUser
    }

    fun scheduleTrackerForGig(
            gig: Gig
    ) = GlobalScope.launch {

        val profile = try {

            if(firebaseUser != null) {
                profileFirebaseRepository.getProfileDataIfExist(firebaseUser!!.uid)
            } else {
                null
            }
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
                userName = profile?.name,
                tradingName = gig.getFullCompanyName() ?: "Gigforce"
        )
    }

//    private suspend fun scheduleTrackers() {
//        try {
//
//            val gigs = gigsRepository.getOngoingAndUpcomingGigsFor(LocalDate.now())
//            val profile = profileFirebaseRepository.getProfileDataIfExist()
//
//            for (gig in gigs) {
//
//                val possibleAlarmsBtw = TrackingScheduleCalculator.getPossibleAlarmTimesBetween(
//                        start = gig.startDateTime,
//                        end = gig.endDateTime
//                )
//                scheduleAlarmsBetween(
//                        alarmTimes = possibleAlarmsBtw,
//                        gigId = gig.gigId,
//                        userName = profile?.name
//                )
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            CrashlyticsLogger.e(
//                    TAG,
//                    "Scheduling Alarms",
//                    e
//            )
//        }
//    }


    private fun scheduleAlarmsBetween(
            alarmTimes: List<LocalDateTime>,
            gigId: String,
            tradingName : String,
            userName: String?

    ) {
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        //Preparing Intent Extras
        val intent = Intent(context, TrackingService::class.java).apply {
            action = TrackingConstants.ACTION_START_OR_RESUME_SERVICE
            this.putExtra(TrackingConstants.SERVICE_INTENT_EXTRA_GIG_ID, gigId)
            this.putExtra(TrackingConstants.SERVICE_INTENT_EXTRA_USER_NAME, userName)
            this.putExtra(TrackingConstants.SERVICE_INTENT_EXTRA_TRADING_NAME, tradingName)
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