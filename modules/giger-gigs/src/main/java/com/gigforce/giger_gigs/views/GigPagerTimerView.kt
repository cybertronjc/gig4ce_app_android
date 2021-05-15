package com.gigforce.giger_gigs.views

import android.content.Context
import android.os.CountDownTimer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import com.gigforce.giger_gigs.models.GigStatus
import com.gigforce.giger_gigs.models.Gig
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.toLocalDateTime
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.R
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
) : FrameLayout(
    context,
    attrs
) {

    //Views
    private lateinit var rootCardView: MaterialCardView

    private lateinit var gigDateTV: TextView
    private lateinit var gigTimerTV: TextView
    private lateinit var gigCheckInTimeTV: TextView
    private lateinit var gigStatusCardView: GigStatusCardView

    private lateinit var gigTimerAndDetailsLayout: View
    private lateinit var gigAttendanceDetailsLayout: View

    private lateinit var checkInTimeTV: TextView
    private lateinit var checkOutTimeTV: TextView

    private val gigDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    init {
        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(
            R.layout.fragment_gig_page_2_timer_layout,
            this,
            true
        )
        findViews(view)
    }

    private fun findViews(view: View) {
        rootCardView = view.findViewById(R.id.timer_root_card_layout)

        gigTimerAndDetailsLayout = view.findViewById(R.id.timer_and_attendance_layout)
        gigAttendanceDetailsLayout = view.findViewById(R.id.gig_attendance_details_layout)

        gigDateTV = view.findViewById(R.id.gig_date_tv)
        gigTimerTV = view.findViewById(R.id.gig_timer_tv)
        gigCheckInTimeTV = view.findViewById(R.id.gig_checkin_time_tv)
        gigStatusCardView = view.findViewById(R.id.gig_status_card_view)

        checkInTimeTV = view.findViewById(R.id.presentGigpunchInTimeTV)
        checkOutTimeTV = view.findViewById(R.id.presentGigpunchOutTimeTV)
    }

    fun setGigData(gig: Gig) {
        resetTimer()
        val status = GigStatus.fromGig(gig)

        gigStatusCardView.setGigData(status)
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

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_upcoming_pink, null)
//        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "Checkin Not Marked"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showMissedGigDetails(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_no_show_red, null)
//        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "You've missed this gig"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showNoShowGigDetails(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_declined_red, null)
//        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "Check-in Pending"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showCompletedGigDetails(gig: Gig) {
        gigTimerAndDetailsLayout.gone()
        gigAttendanceDetailsLayout.visible()

//        rootCardView.setCardBackgroundColor(ColorStateList.valueOf(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_completed_grey ,null)
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

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_upcoming_pink, null)
//        )

        val daysDiff = Duration.between(
            LocalDate.now().atStartOfDay(),
            gig.startDateTime.toLocalDateTime()
        ).toDays()

        if (daysDiff > 1) {
            //Show Date only
            gigTimerTV.text = "$daysDiff Days"
        } else {
            startCountDownTimer(gig.startDateTime.toDate())
        }

        gigCheckInTimeTV.text = "Left for the gig start"
        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)

    }

    private var countDownTimer: CountDownTimer? = null
    private fun startCountDownTimer(gigStartTime: Date) {

        if (countDownTimer == null) {

            val currentTime = Date().time
            val diffInMillisec: Long = gigStartTime.time - currentTime

            countDownTimer = object : CountDownTimer(diffInMillisec, 1000L) {
                override fun onTick(millisUntilFinished: Long) {

                    val diffInHours: Long = if (millisUntilFinished > 3600000)
                        TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                    else
                        0L
                    val diffInMin: Long = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                    val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60

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

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_ongoing_green, null)
//        )

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

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_declined_red, null)
//        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = "Declined Reason : ${gig.declineReason}"

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun showGigCancelled(gig: Gig) {
        gigAttendanceDetailsLayout.gone()
        gigTimerAndDetailsLayout.visible()

//        rootCardView.setCardBackgroundColor(
//                ResourcesCompat.getColor(resources, R.color.gig_timer_silver_light, null)
//        )
        gigTimerTV.text = "- -hrs: - -mins"
        gigCheckInTimeTV.text = gig.cancellationReason

        gigDateTV.text = formatGigDateForTimer(gig.startDateTime)
    }

    private fun formatGigDateForTimer(startDateTime: Timestamp): String {

        val daysDiff = Duration.between(
            startDateTime.toLocalDateTime(),
            LocalDate.now().atStartOfDay()
        ).toDays()

        if (daysDiff == 0L) {
            return "Today, ${gigDateFormat.format(startDateTime.toDate())}"
        } else if (daysDiff == -1L) {
            return "Tomorrow, ${gigDateFormat.format(startDateTime.toDate())}"
        } else if (daysDiff == 1L) {
            return "Yesterday, ${gigDateFormat.format(startDateTime.toDate())}"
        } else {
            return gigDateFormat.format(startDateTime.toDate())
        }
    }


}