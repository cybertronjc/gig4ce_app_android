package com.gigforce.app.modules.gigPage2.views

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.toLocalDateTime
import com.gigforce.app.core.visible
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.modules.gigPage2.models.GigStatus
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit

class GigPagerTimerView(
        context: Context,
        attrs: AttributeSet
) : MaterialCardView(
        context,
        attrs
) {

    //Views
    private lateinit var rootCardView: MaterialCardView

    private lateinit var gigDateTV: TextView
    private lateinit var gigTimerTV: TextView
    private lateinit var gigCheckInTimeTV: TextView
    private lateinit var gigStatusIV: ImageView
    private lateinit var gigStatusTV: TextView

    private lateinit var gigTimerAndDetailsLayout: View
    private lateinit var gigAttendanceDetailsLayout: View

    private lateinit var checkInTimeTV: TextView
    private lateinit var checkOutTimeTV: TextView

    private val gigDateFormat = SimpleDateFormat("dd/MMM/yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    init {
        val layoutInflater = LayoutInflater.from(context)
        layoutInflater.inflate(
                R.layout.fragment_gig_page_2_timer_layout,
                this,
                true
        )
        findViews()
    }

    private fun findViews() {
        rootCardView = findViewById(R.id.gig_page_timer_layout)

        gigTimerAndDetailsLayout = findViewById(R.id.timer_and_attendance_layout)
        gigAttendanceDetailsLayout = findViewById(R.id.gig_attendance_details_layout)

        gigDateTV = findViewById(R.id.gig_date_tv)
        gigTimerTV = findViewById(R.id.gig_timer_tv)
        gigCheckInTimeTV = findViewById(R.id.gig_checkin_time_tv)
        gigStatusTV = findViewById(R.id.gig_status_tv)
        gigStatusIV = findViewById(R.id.gig_status_iv)

        checkInTimeTV = findViewById(R.id.presentGigpunchInTimeTV)
        checkOutTimeTV = findViewById(R.id.presentGigpunchOutTimeTV)
    }

    fun setGigData(gig: Gig) {
        resetTimer()
        val status = GigStatus.fromGig(gig)

        gigStatusTV.text = status.getStatusCapitalized()
        Glide.with(context).load(status.getIconForStatus()).into(gigStatusIV)

        when (status) {
            GigStatus.UPCOMING -> showUpcomingGigDetails(gig)
            GigStatus.DECLINED -> showGigDeclined(gig)
            GigStatus.CANCELLED -> showGigCancelled(gig)
            GigStatus.ONGOING -> showOngoingGigDetails(gig)
            GigStatus.PENDING -> showPendingGigDetails(gig)
            GigStatus.NO_SHOW -> showNoShowGigDetails(gig)
            GigStatus.COMPLETED -> showCompletedGigDetails(gig)
            GigStatus.MISSED -> showMissedGigDetails(gig)
        }
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    private fun showPendingGigDetails(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_no_show_red, null)
        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "Checkin Not Marked"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showMissedGigDetails(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_no_show_red, null)
        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "You've missed this gig"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showNoShowGigDetails(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_no_show_red, null)
        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "Check-in Pending"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showCompletedGigDetails(gig: Gig) {
        gigTimerAndDetailsLayout.gone()
        gigAttendanceDetailsLayout.visible()

//        rootCardView.setCardBackgroundColor(ColorStateList.valueOf(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_upcoming_pink, null)
//        ))


        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)

        if (gig.attendance == null || gig.attendance?.checkInTime == null) {

            val e = IllegalStateException("status is completed but attendance null ,attendance null =  ${gig.attendance == null} or checkin null = ${gig.attendance?.checkInTime == null} or checkout null = ${gig.attendance?.checkOutTime == null}")
            FirebaseCrashlytics.getInstance().apply {
                this.log("Gig Id : ${gig.gigId}")
                this.recordException(e)
            }
            throw e
        }

        checkInTimeTV.text = timeFormatter.format(gig.attendance!!.checkInTime!!)
        checkOutTimeTV.text = timeFormatter.format(gig.attendance!!.checkOutTime!!)
    }

    private fun showUpcomingGigDetails(
            gig: Gig
    ) {

        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_upcoming_pink, null)
        )

        val daysDiff = Duration.between(
                gig.startDateTime.toLocalDateTime(),
                LocalDate.now().atStartOfDay()
        ).toDays()

        if (daysDiff > 1) {
            //Show Date only
        } else {
            startCountDownTimer(gig.startDateTime.toDate())
        }

        gigCheckInTimeTV.text = "Left for the gig start"
        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)


//        if (gig.isCheckInAndCheckOutMarked()) {
//            val checkInTime = gig.attendance!!.checkInTime!!
//            val checkOutTime = gig.attendance!!.checkOutTime!!
//
//            val diffInMillisec: Long = checkOutTime.time - checkInTime.time
//            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
//            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60
//
//            gig_timer_tv.text = "$diffInHours Hrs : $diffInMin Mins"
//            val checkoutTime = gig.attendance!!.checkOutTime
//            gig_checkin_time_tv.text = "${timeFormatter.format(checkInTime)} - ${
//                timeFormatter.format(checkoutTime)
//            }"
//        } else if (gig.isCheckInMarked()) {
//            val gigStartDateTime = gig.startDateTime.toDate()
//            val currentTime = Date().time
//
//            val diffInMillisec: Long = currentTime - gigStartDateTime.time
//            val diffInHours: Long = TimeUnit.MILLISECONDS.toHours(diffInMillisec)
//            val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60
//
//            gig_timer_tv.text = "No check-out marked"
//            gig_checkin_time_tv.text = "${timeFormatter.format(gigStartDateTime)} -"
//        } else {
//            gig_timer_tv.text = "No Check-in"
//            gig_checkin_time_tv.gone()
//        }

    }

    private var countDownTimer: CountDownTimer? = null
    private fun startCountDownTimer(gigStartTime: Date) {

        if (countDownTimer == null) {

            val currentTime = Date().time
            val diffInMillisec: Long = gigStartTime.time - currentTime

            countDownTimer = object : CountDownTimer(diffInMillisec, 1000L) {

                override fun onTick(millisUntilFinished: Long) {
                    val diffInHours: Long = if (diffInMillisec > 3600000)
                        TimeUnit.MILLISECONDS.toHours(diffInMillisec)
                    else
                        0L
                    val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(diffInMillisec) % 60
                    val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(diffInMillisec) % 60

                    gigTimerTV.text = "$diffInHours Hrs : $diffInMin Mins : $diffInSec Sec"
                }

                override fun onFinish() {

                }
            }
            countDownTimer?.start()
        }
    }


    private fun showOngoingGigDetails(gig: Gig) {
        gigTimerAndDetailsLayout.gone()
        gigAttendanceDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_ongoing_green, null)
        )

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)

        if (gig.attendance == null || gig.attendance?.checkInTime == null) {
            throw IllegalStateException("status is going but attendance or checkin-time was null")
        }

        checkInTimeTV.text = timeFormatter.format(gig.attendance!!.checkInTime!!)
        checkOutTimeTV.text = "--:--"
    }

    private fun showGigDeclined(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_silver_light, null)
        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = gig.cancellationReason

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showGigCancelled(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

        rootCardView.setCardBackgroundColor(
                ResourcesCompat.getColor(resources, R.color.gig_timer_declined_red, null)
        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = gig.declineReason

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun formatGigDateForTimer(startDateTime: Timestamp): String {

        val daysDiff = Duration.between(
                startDateTime.toLocalDateTime(),
                LocalDate.now().atStartOfDay()
        ).toDays()

        if (daysDiff == 0L) {
            return "Today, ${gigDateFormat.format(startDateTime.toDate())}"
        } else if (daysDiff == 1L) {
            return "Tomorrow, ${gigDateFormat.format(startDateTime.toDate())}"
        } else if (daysDiff == -1L) {
            return "Yesterday, ${gigDateFormat.format(startDateTime.toDate())}"
        } else {
            return gigDateFormat.format(startDateTime.toDate())
        }
    }


}