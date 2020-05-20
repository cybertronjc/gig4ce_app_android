package com.gigforce.app.modules.roster

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.LifecycleOwner
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import com.gigforce.app.modules.roster.models.Gig
import com.github.pwittchen.swipe.library.rx2.Swipe
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.item_roster_day.view.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class RosterDayFragment: RosterBaseFragment() {

    var TAG: String = "DEBUGDAYVIEW"

    // To fake infinite scroll on day view. We set the adapter array size to 10000
    // and start from 5000
    var lastViewPosition = 5000

    @RequiresApi(Build.VERSION_CODES.O)
    var activeDateTime = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var actualDateTime = LocalDateTime.now()

    var upcomingGigs: ArrayList<Gig> = ArrayList<Gig>()
    var unavailableCards: ArrayList<String> = ArrayList()

    lateinit var hourviewPageChangeCallBack: ViewPager2.OnPageChangeCallback



//    val currentDateTime = LocalDateTime.now()

    val swipe = Swipe()

//    @RequiresApi(Build.VERSION_CODES.O)
//    var handler = Handler() { msg ->
//        var datetime = LocalDateTime.now()
//        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
//        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
//        layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
//        current_time_divider.requestLayout()
//        true
//    }

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

        var sampleGigUpcoming = Gig(startHour = 13, startMinute = 30, duration = 3.5F)
        var sampleGigCompleted = Gig(startHour = 9, duration = 4.0F)

        upcomingGigs.add(sampleGigUpcoming)

        initialize()
        setListeners()
//

//
//        addUnAvailableCard(5, 3.0F)
//
//        addCompletedGigCard(sampleGigCompleted)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        val hourViewAdapter = HourViewAdapter(requireActivity(), 10000)
        hourview_viewpager .adapter = hourViewAdapter
        hourview_viewpager.setCurrentItem(lastViewPosition, false)

        rosterViewModel.currentDateTime.observe( viewLifecycleOwner, androidx.lifecycle.Observer { it ->
            activeDateTime = it
            if (it.year != top_bar.year)
                top_bar.year = it.year
            if (it.monthValue - 1 != top_bar.month)
                top_bar.month = it.monthValue - 1
            top_bar.date = it.dayOfMonth
            top_bar.day = it.dayOfWeek.value - 1

            top_bar.isCurrentDay = isSameDate(it, actualDateTime)

            setCurrentTimeVisibility()

            //addUpcomingGigCard(upcomingGigs[0])

        })

        setCurrentTimeDivider()
        scheduleCurrentTimerUpdate()

        top_bar.month_selector.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val monthGap = position - (activeDateTime.monthValue - 1)
                if (monthGap < 0)
                    activeDateTime = activeDateTime.minusMonths((-1 * monthGap).toLong())
                else
                    activeDateTime = activeDateTime.plusMonths((monthGap).toLong())
                rosterViewModel.currentDateTime.setValue(activeDateTime)
            }
        }

        //initializeHourViews()
        //setCurrentTimeDivider()
        //scheduleCurrentTimerUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setCurrentTimeVisibility() {

        if (isSameDate(activeDateTime, actualDateTime))
            current_time_divider.visibility = View.VISIBLE
        else
            current_time_divider.visibility = View.INVISIBLE
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleCurrentTimerUpdate() {
        var handler = Handler() { msg ->
            var datetime = LocalDateTime.now()
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
        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
        current_time_divider.requestLayout()
    }

    private fun setListeners() {
        hourviewPageChangeCallBack = object: ViewPager2.OnPageChangeCallback() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position < lastViewPosition) {
                    rosterViewModel.currentDateTime.setValue(activeDateTime.minusDays(1))
                } else if (position > lastViewPosition) {
                    rosterViewModel.currentDateTime.setValue(activeDateTime.plusDays(1))
                }
                lastViewPosition = position
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)

                if (state == ViewPager2.SCROLL_STATE_IDLE) {
                    setCurrentTimeVisibility()
                } else {
                    current_time_divider.visibility = View.INVISIBLE
                }
            }
        }

        hourview_viewpager.registerOnPageChangeCallback(hourviewPageChangeCallBack)
    }

    override fun onDestroy() {
        super.onDestroy()
        hourview_viewpager.unregisterOnPageChangeCallback(hourviewPageChangeCallBack)
    }

    private fun addUpcomingGigCard(gig: Gig) {
        val upcomingCard = UpcomingGigCard(requireContext())

        hours_view.addView(upcomingCard)

        upcomingCard.id = View.generateViewId()
        upcomingCard.startHour = gig.startHour
        upcomingCard.startMinute = gig.startMinute
        upcomingCard.duration = gig.duration
        upcomingCard.cardHeight = (itemHeight * gig.duration).toInt().px

        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
        upcomingCard.setLayoutParams(params)

        upcomingCard.tag = "gig_" + upcomingCard.startHour.toString() + "_" + upcomingCard.startMinute.toString()
        var marginTop = (upcomingCard.startHour * itemHeight + ((upcomingCard.startMinute/60.0F)*itemHeight).toInt()).px

        var constraintSet = ConstraintSet()
        constraintSet.clone(hours_view)
        constraintSet.connect(upcomingCard.id, ConstraintSet.START, start_guideline_day.id, ConstraintSet.START, marginCardStart)
        constraintSet.connect(upcomingCard.id, ConstraintSet.END, end_guideline_day.id, ConstraintSet.START, marginCardEnd)
        constraintSet.connect(upcomingCard.id, ConstraintSet.TOP, hours_view.id, ConstraintSet.TOP, marginTop)
        constraintSet.applyTo(hours_view)

        upcomingCard.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on upcoming card", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun addUnAvailableCard(startHour: Int, duration: Float) {
//        // Sample attachment of unavailable card
//        val unavailableCard = UnavailableCard(this.requireContext())
//        unavailableCard.id = View.generateViewId()
//        unavailableCard.unavailableStartHour = startHour
//        unavailableCard.unavailableStartMinute = 0
//        unavailableCard.unavailableDuration = duration
//        unavailableCard.tag = "gig_" + unavailableCard.unavailableStartHour.toString() + "_" + unavailableCard.unavailableStartMinute
//        unavailableCard.cardHeight = (itemHeight * duration).toInt().px
//
//        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//        unavailableCard.setLayoutParams(params)
//        val marginTop = (unavailableCard.unavailableStartHour * itemHeight + ((unavailableCard.unavailableStartMinute/60.0F)*itemHeight).toInt()).px
//
//        day_times.addView(unavailableCard)
//
//        unavailableCards.add(unavailableCard.tag.toString())
//
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(day_times)
//        constraintSet.connect(unavailableCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart)
//        constraintSet.connect(unavailableCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START)
//        constraintSet.connect(unavailableCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
//        constraintSet.applyTo(day_times)
//
//        unavailableCard.setOnClickListener {
//            Toast.makeText(requireContext(), "Clicked on unavailable card", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun addCompletedGigCard(gig: Gig) {
//        var completedGigCard = CompletedGigCard(requireContext())
//        completedGigCard.gigStartHour = gig.startHour
//        completedGigCard.gigStartMinute = gig.startMinute
//        completedGigCard.gigDuration = gig.duration
//        completedGigCard.cardHeight = (itemHeight * gig.duration).toInt().px
//        completedGigCard.id = View.generateViewId()
//
//        var params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT)
//        completedGigCard.setLayoutParams(params)
//        completedGigCard.tag = "gig_" + completedGigCard.gigStartHour.toString() + "_" + completedGigCard.gigStartMinute.toString()
//        val marginTop = (completedGigCard.gigStartHour * itemHeight + ((completedGigCard.gigStartMinute/60.0F)*itemHeight).toInt()).px
//
//        day_times.addView(completedGigCard)
//
//        val constraintSet = ConstraintSet()
//        constraintSet.clone(day_times)
//        constraintSet.connect(completedGigCard.id, ConstraintSet.START, start_guideline.id, ConstraintSet.START, marginCardStart - 16.px)
//        constraintSet.connect(completedGigCard.id, ConstraintSet.END, end_guideline.id, ConstraintSet.START, marginCardEnd)
//        constraintSet.connect(completedGigCard.id, ConstraintSet.TOP, day_times.id, ConstraintSet.TOP, marginTop)
//        constraintSet.applyTo(day_times)
//
//        completedGigCard.setOnClickListener {
//            Toast.makeText(requireContext(), "Clicked on completed card", Toast.LENGTH_SHORT).show()
//        }
//
//    }
//
//    private fun setListeners() {
//        top_bar.available_toggle.setOnClickListener {
//            toggleAvailability()
//
//        }
//
//    }
//
//    private fun toggleAvailability() {
//        top_bar.isAvailable = !top_bar.isAvailable
//
//        // disable hours
//        var hourView: HourRow
//        for (idx in 1..24) {
//            hourView = day_times.findViewWithTag("hour_$idx")
//            hourView.isDisabled = !hourView.isDisabled
//        }
//        if (!top_bar.isAvailable) {
//            if (upcomingGigs.size > 0) {
//                showGigsTodayWarning()
//            }
//        }
//    }
//
//    private fun removeUnavailableCards() {
//        for (tag in unavailableCards) {
//            var view = day_times.findViewWithTag<UnavailableCard>(tag)
//            day_times.removeView(view)
//        }

   // }

//    private fun removeUpcomingGigsCards() {
//        for (gig in upcomingGigs) {
//            var tag = "gig_" + gig.startHour + "_" + gig.startMinute
//            var view = day_times.findViewWithTag<UpcomingGigCard>(tag)
//            day_times.removeView(view)
//        }
//    }
//
//    private fun showGigsTodayWarning() {
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(false)
//        dialog.setContentView(R.layout.gigs_today_warning_dialog)
//
//        dialog.dialog_content.setText(
//            "You have " + upcomingGigs.size.toString() + " Gig(s) active on the day. These gigs will get canceled as well."
//        )
//
//        dialog.cancel.setOnClickListener {
//            toggleAvailability()
//            dialog .dismiss()
//        }
//
//        dialog.yes.setOnClickListener {
//            Toast.makeText(requireContext(), "Clicked on YeS", Toast.LENGTH_SHORT).show()
//            showReasonForGigCancel()
//            dialog .dismiss()
//        }
//
//        dialog.show()
//    }
//
//    private fun showReasonForGigCancel() {
//        val dialog = Dialog(requireContext())
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
//        dialog.setCancelable(false)
//        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)
//
//        var selectedText = ""
//
//        dialog.cancel_options.setOnCheckedChangeListener ( RadioGroup.OnCheckedChangeListener { group, checkedId ->
//            selectedText = dialog.findViewById<RadioButton>(checkedId).text.toString()
//        })
//
//        dialog.submit_button.setOnClickListener {
//            Toast.makeText(requireContext(), "selected option " + selectedText, Toast.LENGTH_SHORT).show()
//            removeUnavailableCards()
//            removeUpcomingGigsCards()
//            dialog .dismiss()
//        }
//
//        dialog.cancel_button.setOnClickListener {
//            toggleAvailability()
//            dialog .dismiss()
//        }
//
//        dialog.show()
//    }

}
