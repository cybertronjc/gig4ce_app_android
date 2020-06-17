package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.gigforce.app.R
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.completed_gig_card.view.*
import kotlinx.android.synthetic.main.dropdown_spinner.*
import kotlinx.android.synthetic.main.item_roster_day.view.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.view.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.view.day_text
import kotlinx.android.synthetic.main.upcoming_gig_card.view.*
import kotlinx.android.synthetic.main.upcoming_gig_card.view.gig_title
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.min

class HourViewFragment: RosterBaseFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    var actualDateTime = LocalDateTime.now()
    lateinit var activeDateTime: LocalDateTime

    var dayTag: String = ""

    val hourIds = ArrayList<Int>()

    var viewInitialized: Boolean = false

    var timer = Timer()

    // This is used for card alignment of gig complete card
    var cardStartPadding = 16.px

    val times = ArrayList<String>(listOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
    "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
    "21:00", "22:00", "23:00", "24:00"))

    var timeToHourMap = HashMap<String, Int>(
            hashMapOf(Pair("00:00", 0), Pair("01:00", 1), Pair("02:00", 2), Pair("03:00", 3),
                    Pair("04:00", 4), Pair("05:00", 5), Pair("06:00", 6), Pair("07:00", 7),
                    Pair("08:00", 8), Pair("09:00", 9), Pair("10:00", 10), Pair("11:00", 11),
                    Pair("12:00", 12), Pair("13:00", 13), Pair("14:00", 14), Pair("15:00", 15),
                    Pair("16:00", 16), Pair("17:00", 17), Pair("18:00", 18), Pair("19:00", 19),
                    Pair("20:00", 20), Pair("21:00", 21), Pair("22:00", 22), Pair("23:00", 23),
                    Pair("24:00", 24)
            )
    )

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

        setHourVisibility(day_times, activeDateTime, actualDateTime)

        if (isSameDate(activeDateTime, actualDateTime)) {
            setCurrentTimeDivider()
            scheduleCurrentTimerUpdate()
        }

        addGigCards()

    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleCurrentTimerUpdate() {
        val handler = Handler() { msg ->
            val datetime = LocalDateTime.now()
            val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
            val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
            current_time_divider.requestLayout()
            true
        }
        timer.scheduleAtFixedRate(object: TimerTask() {
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
        dayTag = "${activeDateTime.year}${activeDateTime.monthValue.toString().format("%02c")}${activeDateTime.dayOfMonth}"
        rosterViewModel.gigsQuery.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag, it))
            for (gig in upcomingGigs)
                addUpcomingGigCard(gig)

            completedGigs.addAll(rosterViewModel.getCompletedGigsByDayTag(dayTag, it))
            for (gig in completedGigs)
                addCompletedGigCard(gig)
        })

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
        upcomingCard.gig_title.text = gig.title
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
        completedGigCard.gig_title.text = gig.title
        completedGigCard.cardHeight = (itemHeight * gig.duration).toInt().px
        completedGigCard.id = View.generateViewId()
        completedGigCard.gigSuccess = gig.isGigCompleted
        completedGigCard.paymentSuccess = gig.isPaymentDone

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

        // outline the selected Hour
        setHourOUtline(startIndex, endIndex)

        // set collapsed view elements
        setBsCollapsedDayTimeText(startIndex, endIndex)
        // set expanded view elements
        setBsExpandedDayTimeText(startIndex, endIndex)
        // make day availability toggle work in bottom sheet
        setBsExpandedAvailabilityToggle()
        // set upcoming cards for expanded bottom sheet
        setBsExpandedUpcomingGigs(startIndex+1, endIndex+1)

        rosterViewModel.UnavailableBS.bs_close_button.setOnClickListener {
            rosterViewModel.bsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
            day_times.removeView(day_times.findViewWithTag<HourOutline>("selected_time"))
        }

        // show bottom sheet in collapsed mode
        rosterViewModel.bsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED

        // This is to stop hours covered by bottom sheet from receiving click
        rosterViewModel.UnavailableBS.setOnClickListener {  }
    }

    fun setHourOUtline(startIndex: Int, endIndex: Int) {
        val bottom_sheet = rosterViewModel.UnavailableBS

        // remove existing outline if any
        day_times.removeView(bottom_sheet.findViewWithTag<HourOutline>("selected_time"))

        // add new outline
        val outline = HourOutline(requireContext())
        outline.id = View.generateViewId()
        outline.tag = "selected_time"

        // initialize outline attrs
        // The minimum selectable unit is hour right now.
        outline.startHour = startIndex + 1
        outline.startMinute = 0
        outline.endHour = endIndex + 1
        outline.endMinute = 0

        day_times.addView(outline)

        // TODO: Check why adding the end constraint results in unexpected alignment
        val constraintSet = ConstraintSet()
        constraintSet.clone(day_times)
        constraintSet.connect(outline.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP)
        constraintSet.connect(outline.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart - cardStartPadding)
//      constraintSet.connect(outline.id, ConstraintSet.END, end_guideline.id, ConstraintSet.END)
        constraintSet.applyTo(day_times)

        outline.resetHeightAndTopMargin(itemHeight)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setBsExpandedAvailabilityToggle() {
        rosterViewModel.isDayAvailable.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            rosterViewModel.UnavailableBS.toggle_button.isChecked = it
        })

        rosterViewModel.UnavailableBS.toggle_button.setOnClickListener {
            if (rosterViewModel.UnavailableBS.toggle_button.isChecked) {
                if (upcomingGigs.size > 0)
                    rosterViewModel.isDayAvailable.value = !showGigsTodayWarning(
                            requireContext(), upcomingGigs, day_times
                    )
                else {
                    rosterViewModel.isDayAvailable.value = false
                    allHourInactive(day_times)
                }
            } else {
                rosterViewModel.isDayAvailable.value = true
                setHourVisibility(day_times, activeDateTime, actualDateTime)
            }
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
        val bottom_sheet = rosterViewModel.UnavailableBS

        // for expanded state
        bottom_sheet.start_day_text.setText(
            "${activeDateTime.dayOfWeek.toString().capitalize()}, ${activeDateTime.dayOfMonth} " +
                    "${activeDateTime.month}, ${activeDateTime.year}")

        bottom_sheet.end_day_text.setText(
            "${activeDateTime.dayOfWeek.toString().capitalize()}, ${activeDateTime.dayOfMonth } " +
                    "${activeDateTime.month}, ${activeDateTime.year}")

        if (startIndex == 0)
            bottom_sheet.start_day_time.setText("00:00")
        else
            bottom_sheet.start_day_time.setText("${times[startIndex]}")

        bottom_sheet.end_day_time.setText("${times[endIndex]}")

        bottom_sheet.start_day_time.setOnClickListener {
            val cal = Calendar.getInstance()
            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute, second ->
                bottom_sheet.start_day_time.text = "$hour:$minute"

                // adjust outline as per changed time
                val outline = day_times.findViewWithTag<HourOutline>("selected_time")
                outline.startHour = hour
                outline.startMinute = minute
                outline.resetHeightAndTopMargin(itemHeight)
//                (outline.layoutParams as ViewGroup.MarginLayoutParams).setMargins(marginCardStart-16.px, hour * 70.px + ((minute/60)*70).toInt().px, 0, 0)
//                outline.requestLayout()
                setBsExpandedUpcomingGigs(outline.startHour, outline.endHour)
            }
            TimePickerDialog.newInstance(timeSetListener, timeToHourMap[times[startIndex]]!!, 0, true).show(requireFragmentManager(), "DateTimePicker")
        }
        rosterViewModel.UnavailableBS.end_day_time.setOnClickListener {
            val cal = Calendar.getInstance()

            val timeSetListener = TimePickerDialog.OnTimeSetListener { timePicker, hour, minute, second ->
                rosterViewModel.UnavailableBS.end_day_time.text = "$hour:$minute"
                var outline = day_times.findViewWithTag<HourOutline>("selected_time")
                outline.endHour = hour
                outline.endMinute = minute
                outline.resetHeightAndTopMargin(itemHeight)
                setBsExpandedUpcomingGigs(outline.startHour, outline.endHour)
            }
            TimePickerDialog.newInstance(timeSetListener, timeToHourMap[times[endIndex]]!!, 0, true).show(requireFragmentManager(), "DateTimePicker")
        }
    }

    private fun setBsExpandedUpcomingGigs(startHour: Int, endHour: Int) {
        rosterViewModel.UnavailableBS.assigned_gigs.removeAllViews()
        for (gig in upcomingGigs) {
            if (gig.startHour in startHour..endHour) {
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
    }

    fun removeUpcomingGigCards() {
        for (gig in upcomingGigs) {
            var card = day_times.findViewWithTag<UpcomingGigCard>(gig.tag)
            day_times.removeView(card)
            Log.d("RosterViewModel", "gig card removed")

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
        timer.purge()
    }
}
