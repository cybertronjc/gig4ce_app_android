package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.constraintlayout.widget.Constraints
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import com.google.type.Color
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.vertical_calendar_item.*
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

    // TODO: Modify to get this height from dimens
    val itemHeight = 70

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initialize()
        setListeners()

        var startHour = 13
        var duration = 3.5F
        addUpcomingGigCard(startHour, duration)

        addUnAvailableCard(5, 3.0F)

        addCompletedGigCard(9, 4.0F)
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        initializeHourViews()
        setCurrentTimeDivider()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeHourViews() {
        val times = ArrayList<String>()
        times.addAll(listOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
            "21:00", "22:00", "23:00", "24:00"))

        val datetime = LocalDateTime.now()
        Log.d("DAY", datetime.toString() + " " + datetime.hour + " " + datetime.minute)

        val timeViewGroup = day_times
        val hourIds = ArrayList(listOf(hour_0.id))

        var widget: MaterialCardView
        val constraintSet: ConstraintSet?

        // Adding hourly widgets
        for ((index, time) in times.withIndex()) {
            widget = HourRow(this.requireContext())
            widget.id = View.generateViewId()
            widget.hour = index
            widget.time = time
            widget.setOnClickListener {
                if (!widget.clicked) {
                    widget.clicked = true
                    widget.isChecked = true
                    widget.setCardBackgroundColor(resources.getColor(R.color.red))
                }
                else {
                    widget.clicked = false
                    widget.isChecked = false
                    widget.setCardBackgroundColor(resources.getColor(R.color.fui_transparent))
                }
            }

            timeViewGroup.addView(widget)

            hourIds.add(widget.id)
            Log.d("PreviousID", widget.id.toString())

        }
        // Adding constraints for hourly widgets
        constraintSet = ConstraintSet()
        constraintSet.clone(timeViewGroup)
        for ((index,idx) in hourIds.withIndex()) {
            if (index == 0)
                continue
            constraintSet.connect(idx, ConstraintSet.START, timeViewGroup.id, ConstraintSet.START, 0)
            constraintSet.connect(idx, ConstraintSet.END, timeViewGroup.id, ConstraintSet.END, 0)
            constraintSet.connect(idx, ConstraintSet.TOP, hourIds[index - 1], ConstraintSet.BOTTOM, 0)
            Log.d("Constraint", "applied")
        }
        constraintSet.applyTo(timeViewGroup)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCurrentTimeDivider() {
        val datetime = LocalDateTime.now()
        // set current time divider
        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(0, marginTop, 0, 0)
        current_time_divider.requestLayout()
    }

    private fun addUpcomingGigCard(startHour: Int, duration: Float) {
        val upcomingCard = UpcomingGigCard(this.requireContext())
        upcomingCard.id = View.generateViewId()
        upcomingCard.startHour = startHour
        upcomingCard.duration = duration
        upcomingCard.cardHeight = (itemHeight * duration).toInt().px
        var marginTop = (upcomingCard.startHour * itemHeight).px

        day_times.addView(upcomingCard)

        var constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(upcomingCard.id, ConstraintSet.START, day_times.id, ConstraintSet.START, 85.px)
        constraintSet.connect(upcomingCard.id, ConstraintSet.END, day_times.id, ConstraintSet.END, 30.px)
        constraintSet.connect(upcomingCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)
    }

    private fun addUnAvailableCard(startHour: Int, duration: Float) {
        // Sample attachment of unavailable card
        val unavailableCard = UnavailableCard(this.requireContext())
        unavailableCard.id = View.generateViewId()
        unavailableCard.unavailableStartHour = startHour
        unavailableCard.unavailableDuration = duration
        unavailableCard.cardHeight = (itemHeight * duration).toInt().px
        val marginTop = (unavailableCard.unavailableStartHour * itemHeight).px

        day_times.addView(unavailableCard)

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(unavailableCard.id, ConstraintSet.START, day_times.id, ConstraintSet.START, 85.px)
        constraintSet.connect(unavailableCard.id, ConstraintSet.END, day_times.id, ConstraintSet.END, 30.px)
        constraintSet.connect(unavailableCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)
    }

    private fun addCompletedGigCard(startHour: Int, duration: Float) {
        var completedGigCard = CompletedGigCard(this.requireContext())
        completedGigCard.gigStartHour = startHour
        completedGigCard.gigDuration = duration
        completedGigCard.cardHeight = (itemHeight * duration).toInt().px
        completedGigCard.id = View.generateViewId()
        val marginTop = (completedGigCard.gigStartHour * itemHeight).px

        day_times.addView(completedGigCard)

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(completedGigCard.id, ConstraintSet.START, day_times.id, ConstraintSet.START, 100.px)
        constraintSet.connect(completedGigCard.id, ConstraintSet.END, day_times.id, ConstraintSet.END, 50.px)
        constraintSet.connect(completedGigCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)
    }

    private fun setListeners() {

    }
}