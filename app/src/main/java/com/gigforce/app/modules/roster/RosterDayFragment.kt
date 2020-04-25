package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import com.gigforce.app.R
import kotlinx.android.synthetic.main.roster_day_fragment.*
import java.time.LocalDateTime
import kotlin.collections.ArrayList

class RosterDayFragment: RosterBaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roster_day_fragment, inflater, container)

        return getFragmentView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        var itemHeight = 70

        var times = ArrayList<String>()
        times.addAll(listOf("00:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
            "21:00", "22:00", "23:00", "24:00"))

        var datetime = LocalDateTime.now()
        Log.d("DAY", datetime.toString() + " " + datetime.hour + " " + datetime.minute)

        var timeviewgroup = day_times
        for ((index, time) in times.withIndex()) {
            var widget = HourRow(this.context!!)
            widget.hour = index
            widget.time = time

            if (index == datetime.hour && datetime.minute < 30) {
                widget.minute = datetime.minute
                widget.isCurrentTime = true
            }
            if (index == datetime.hour + 1 && datetime.minute >= 30) {
                widget.minute = datetime.minute
                widget.isCurrentTime = true
            }
            timeviewgroup.addView(widget)
        }

        // Sample attachment of gig completed card
        var completedGigCard = CompletedGigCard(this.context!!)
        completedGigCard.gigStartHour = 13
        completedGigCard.gigDuration = 4.0F
        completedGigCard.cardHeight = (itemHeight*(4.0F)).toInt().px
        completedGigCard.id = View.generateViewId()
        temp_trial.addView(completedGigCard)
        var layoutparams = completedGigCard.layoutParams as FrameLayout.LayoutParams
        layoutparams.setMargins(85.px, (0.5*itemHeight + completedGigCard.gigStartHour * itemHeight).toInt().px, 0, 0)
        layoutparams.height = completedGigCard.cardHeight
        completedGigCard.layoutParams = layoutparams



        // Sample attachment of unavailable card
        var unavailableCard = UnavailableCard(this.context!!)
        unavailableCard.unavailableStartHour = 5
        unavailableCard.unavailableDuration = 3.0F
        unavailableCard.cardHeight = (itemHeight*(3.0F)).toInt().px
        unavailableCard.id = View.generateViewId()
        temp_trial.addView(unavailableCard)
        layoutparams = unavailableCard.layoutParams as FrameLayout.LayoutParams
        layoutparams.setMargins(85.px, (0.5*itemHeight + unavailableCard.unavailableStartHour * itemHeight).toInt().px, 0, 0)
        layoutparams.height = unavailableCard.cardHeight
        unavailableCard.layoutParams = layoutparams


        // Sample attachment of upcoming gig card
        var upcomingCard = UpcomingGigCard(this.context!!)
        upcomingCard.startHour = 18
        upcomingCard.duration = 3.0F
        upcomingCard.cardHeight = (itemHeight*(3.0F)).toInt().px
        temp_trial.addView(upcomingCard)
        layoutparams = upcomingCard.layoutParams as FrameLayout.LayoutParams
        layoutparams.setMargins(85.px, (0.5*itemHeight + upcomingCard.startHour * itemHeight).toInt().px, 0, 0)
        layoutparams.height = upcomingCard.cardHeight
        upcomingCard.layoutParams = layoutparams
    }
}