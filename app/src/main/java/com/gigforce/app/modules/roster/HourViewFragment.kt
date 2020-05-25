package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewParent
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import kotlinx.android.synthetic.main.item_roster_day.view.*
import kotlinx.android.synthetic.main.layout_home_screen.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.view.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.view.day_text
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class HourViewFragment: RosterBaseFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    var actualDateTime = LocalDateTime.now()
    lateinit var activeDateTime: LocalDateTime

    var dayTag: String = ""

    val hourIds = ArrayList<Int>()

    var viewInitialized: Boolean = false

    val times = ArrayList<String>(listOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
    "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
    "21:00", "22:00", "23:00", "24:00"))

    var upcomingGigs = ArrayList<Gig>()
    var completedGigs = ArrayList<Gig>()

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        fun getInstance(position: Int, activeDateTime: LocalDateTime): Fragment {
            val bundle = Bundle()
            bundle.putInt("position", position)
            bundle.putSerializable("activeDate", activeDateTime)
            val hourViewFragment = HourViewFragment()
            hourViewFragment.arguments = bundle
            return hourViewFragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roster_day_hour_view, inflater, container)
        activeDateTime = LocalDateTime.parse(arguments?.getSerializable("activeDate").toString())
        Log.d("HourView", "Entered Hourly view")
        Log.d("HourView", "Datetime received from Adapter is ${activeDateTime.toString()}")
        return getFragmentView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {

        rosterViewModel.bsBehavior.state = BottomSheetBehavior.STATE_HIDDEN

        initializeHourViews()
        if (isSameDate(activeDateTime, actualDateTime)) {
            todayHourActive()
        } else if (isLessDate(activeDateTime, actualDateTime)) {
            allHourInactive()
        } else {
            allHourActive()
        }

        if (isSameDate(activeDateTime, actualDateTime)) {
            setCurrentTimeDivider()
            scheduleCurrentTimerUpdate()
        }


        addGigCards()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleCurrentTimerUpdate() {
        val handler = Handler() { msg ->
            val datetime = activeDateTime
            val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
            val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
            current_time_divider.requestLayout()
            true
        }
        Timer().scheduleAtFixedRate(object: TimerTask() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun run() {
                Log.d("HOUR VIEW", "HELLO WORLD")

                handler.obtainMessage().sendToTarget()

            }
        }, 0, 1000 * 60)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCurrentTimeDivider() {
        val datetime = LocalDateTime.now()
        // set current time divider
        current_time_divider.visibility = View.VISIBLE
        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
        current_time_divider.requestLayout()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun addGigCards() {
        dayTag = "${activeDateTime.year}${activeDateTime.month}${activeDateTime.dayOfMonth}"
        upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag))
        for (gig in upcomingGigs)
            addUpcomingGigCard(gig)

        completedGigs.addAll(rosterViewModel.getCompletedGigsByDayTag(dayTag))
        for (gig in completedGigs)
            addCompletedGigCard(gig)
    }

    private fun addUpcomingGigCard(gig: Gig) {
        Log.d("HourView", "Upcoming gig add")
        Toast.makeText(requireContext(), "Add upcoming gig called", Toast.LENGTH_SHORT).show()
        val upcomingCard = UpcomingGigCard(requireContext())

        day_times.addView(upcomingCard)

        upcomingCard.id = View.generateViewId()
        upcomingCard.startHour = gig.startHour
        upcomingCard.startMinute = gig.startMinute
        upcomingCard.duration = gig.duration
        upcomingCard.cardHeight = (itemHeight * gig.duration).toInt().px

        val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        upcomingCard.setLayoutParams(params)

        upcomingCard.tag = gig.tag
        val marginTop = (upcomingCard.startHour * itemHeight + ((upcomingCard.startMinute/60.0F)*itemHeight).toInt()).px

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(upcomingCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart)
        constraintSet.connect(upcomingCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START, marginCardEnd)
        constraintSet.connect(upcomingCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)

        upcomingCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on upcoming card", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCompletedGigCard(gig: Gig) {
        val completedGigCard = CompletedGigCard(requireContext())
        completedGigCard.gigStartHour = gig.startHour
        completedGigCard.gigStartMinute = gig.startMinute
        completedGigCard.gigDuration = gig.duration
        completedGigCard.cardHeight = (itemHeight * gig.duration).toInt().px
        completedGigCard.id = View.generateViewId()

        val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        completedGigCard.setLayoutParams(params)
        completedGigCard.tag = gig.tag
        val marginTop = (completedGigCard.gigStartHour * itemHeight + ((completedGigCard.gigStartMinute/60.0F)*itemHeight).toInt()).px

        day_times.addView(completedGigCard)

        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(completedGigCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart - 16.px)
        constraintSet.connect(completedGigCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START, marginCardEnd)
        constraintSet.connect(completedGigCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(day_times)

        completedGigCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on completed card", Toast.LENGTH_SHORT).show()
        }

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeHourViews() {

        val timeViewGroup = day_times
        var widget: MaterialCardView
        val constraintSet: ConstraintSet?

        // Adding hourly widgets
        for ((index, time) in times.withIndex()) {
            widget = HourRow(this.requireContext())
            widget.id = View.generateViewId()
            widget.hour = index + 1
            widget.time = time

            timeViewGroup.addView(widget)

            // set width to match parent
            widget.layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            widget.requestLayout()

            widget.top_half.setOnClickListener {
                setAndShowBottomSheet(index-1, index)
           }

            widget.bottom_half.setOnClickListener {
                setAndShowBottomSheet(index, index+1)
            }

            hourIds.add(widget.id)
            Log.d("PreviousID", widget.id.toString())

        }

        // Adding constraints for hourly widgets
        constraintSet = ConstraintSet()
        constraintSet.clone(timeViewGroup)
        for ((index,idx) in hourIds.withIndex()) {
            if (index == 0) {
                constraintSet.connect(idx, ConstraintSet.TOP, hour_0.id, ConstraintSet.BOTTOM, 0)
            } else {
                constraintSet.connect(idx, ConstraintSet.TOP, hourIds[index - 1], ConstraintSet.BOTTOM, 0)
            }
            constraintSet.connect(idx, ConstraintSet.START, start_guideline.id, ConstraintSet.START)
            constraintSet.connect(idx, ConstraintSet.END, end_guideline.id, ConstraintSet.START)

            Log.d("Constraint", "applied")
        }
        constraintSet.applyTo(timeViewGroup)

        viewInitialized = true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setAndShowBottomSheet(startIndex: Int, endIndex: Int) {
        rosterViewModel.bsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        setBsCollapsedDayTimeText(startIndex, endIndex)
        setBsExpandedDayTimeText(startIndex, endIndex)
        setBsExpandedAvailabilityToggle()
        setBsExpandedUpcomingGigs()


    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBsExpandedAvailabilityToggle() {
        rosterViewModel.UnavailableBS.toggle_button.setOnClickListener {

            //rosterViewModel.topBar.isAvailable = !rosterViewModel.topBar.isAvailable
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBsCollapsedDayTimeText(startIndex: Int, endIndex: Int) {
        // set day text
        if (isSameDate(activeDateTime, actualDateTime))
            rosterViewModel.UnavailableBS.day_text.setText("Today")
        else if (isSameDate(activeDateTime.plusDays(1), actualDateTime))
            rosterViewModel.UnavailableBS.day_text.setText("Yesterday")
        else if (isSameDate(activeDateTime.minusDays(1), actualDateTime))
            rosterViewModel.UnavailableBS.day_text.setText("Tomorrow")
        else
            rosterViewModel.UnavailableBS.day_text.setText(activeDateTime.dayOfWeek.toString())

        // set day time
        if (endIndex == 0)
            rosterViewModel.UnavailableBS.time_text.setText("00:00 - ${times[endIndex]}")
        else
            rosterViewModel.UnavailableBS.time_text.setText("${times[startIndex]} - ${times[endIndex]}")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBsExpandedDayTimeText(startIndex: Int, endIndex: Int) {
        // for expanded state
        rosterViewModel.UnavailableBS.start_day_text.setText(
            "${activeDateTime.dayOfWeek.toString().capitalize()}, ${activeDateTime.dayOfMonth} " +
                    "${activeDateTime.month}, ${activeDateTime.year}")

        rosterViewModel.UnavailableBS.end_day_text.setText(
            "${activeDateTime.dayOfWeek.toString().capitalize()}, ${activeDateTime.dayOfMonth } " +
                    "${activeDateTime.month}, ${activeDateTime.year}")

        if (startIndex == 0)
            rosterViewModel.UnavailableBS.start_day_time.setText("00:00")
        else
            rosterViewModel.UnavailableBS.start_day_time.setText("${times[startIndex]}")

        rosterViewModel.UnavailableBS.end_day_time.setText("${times[endIndex]}")
    }

    private fun setBsExpandedUpcomingGigs() {
        rosterViewModel.UnavailableBS.assigned_gigs.removeAllViews()
        for (gig in upcomingGigs) {
            val widget = UpcomingGigCard(requireContext())
            rosterViewModel.UnavailableBS.assigned_gigs.addView(widget)
            widget.id = View.generateViewId()
            widget.startHour = gig.startHour
            widget.startMinute = gig.startMinute
            widget.duration = gig.duration
            widget.cardHeight = 80.px

            (widget.layoutParams as ViewGroup.MarginLayoutParams).setMargins(0, 16.px, 0, 0)
            widget.requestLayout()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun todayHourActive() {
        for (idx in hourIds) {
            var widget = day_times.findViewById<HourRow>(idx)
            if (widget.hour <= activeDateTime.hour) {
                Log.d("HOURVIEW", "inactive hour")
                widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
                widget.isClickable = false
            } else {
                widget.item_time.setTextColor(resources.getColor(R.color.black))
                widget.isClickable = true
            }
        }
    }

    private fun allHourInactive() {
        for (idx in hourIds) {
            var widget = day_times.findViewById<HourRow>(idx)
            Log.d("HOURVIEW", "inactive hour")
            widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
            widget.isClickable = false
        }
    }

    private fun allHourActive() {
        for (idx in hourIds) {
            var widget = day_times.findViewById<HourRow>(idx)
            Log.d("HOURVIEW", "inactive hour")
            widget.item_time.setTextColor(resources.getColor(R.color.black))
            widget.isClickable = true
        }
    }

    fun removeUpcomingGigCards() {
        for (gig in upcomingGigs) {
            var card = day_times.findViewWithTag<UpcomingGigCard>(gig.tag)
            day_times.removeView(card)
            Log.d("RosterViewModel", "gig card removed")

        }
    }

}
