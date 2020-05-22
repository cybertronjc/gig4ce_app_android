package com.gigforce.app.modules.roster

import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class RosterDayFragment: RosterBaseFragment() {

    // To fake infinite scroll on day view. We set the adapter array size to 10000
    // and start from 5000
    var lastViewPosition = 5000

    private var dayTag: String = ""

    @RequiresApi(Build.VERSION_CODES.O)
    var activeDateTime = LocalDateTime.now()

    @RequiresApi(Build.VERSION_CODES.O)
    var actualDateTime = LocalDateTime.now()

    private var upcomingGigs: ArrayList<Gig> = ArrayList<Gig>()
    private var completedGigs: ArrayList<Gig> = ArrayList<Gig>()
    var unavailableCards: ArrayList<String> = ArrayList()

    lateinit var hourviewPageChangeCallBack: ViewPager2.OnPageChangeCallback

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

        initialize()
        setListeners()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        initializeBottomSheet()
        attachHourViewAdapter()
        attachCurrentDateTimeChangeObserver()
        attachTopBarMonthChangeListener()
        setCurrentTimeDivider()
        scheduleCurrentTimerUpdate()
    }

    @RequiresApi(Build.VERSION_CODES.O)
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

        bs_close_button.setOnClickListener {
            rosterViewModel.bsBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        }

        top_bar.available_toggle.setOnClickListener {
            toggleAvailability()
        }
    }

    private fun attachTopBarMonthChangeListener() {

        top_bar.month_selector.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
               // TODO("Not yet implemented")
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val monthGap = position - (activeDateTime.monthValue - 1)
                val newDateTime: LocalDateTime
                val dateDifference: Int

                newDateTime = if (monthGap < 0) {
                    activeDateTime.minusMonths((-1*monthGap).toLong())
                } else {
                    activeDateTime.plusMonths(monthGap.toLong())
                }

                dateDifference =
                    java.time.Duration.between(activeDateTime, newDateTime).toDays().toInt()

                hourview_viewpager.setCurrentItem(
                    (hourview_viewpager.currentItem + dateDifference)
                )
                activeDateTime = newDateTime
                rosterViewModel.currentDateTime.setValue(activeDateTime)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun attachCurrentDateTimeChangeObserver() {

        rosterViewModel.currentDateTime.observe( viewLifecycleOwner, androidx.lifecycle.Observer { it ->
            activeDateTime = it
            if (it.year != top_bar.year)
                top_bar.year = it.year
            if (it.monthValue - 1 != top_bar.month)
                top_bar.month = it.monthValue - 1
            top_bar.date = it.dayOfMonth
            top_bar.day = it.dayOfWeek.value - 1

            top_bar.isCurrentDay = isSameDate(it, actualDateTime)
            top_bar.isFutureDate = isMoreDate(it, actualDateTime)

            setCurrentTimeVisibility()

            dayTag = "${activeDateTime.year}${activeDateTime.month}${activeDateTime.dayOfMonth}"

            upcomingGigs.clear()
            upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag))

            completedGigs.clear()
            completedGigs.addAll(rosterViewModel.getCompletedGigsByDayTag(dayTag))

        })
    }

    private fun attachHourViewAdapter() {
        val hourViewAdapter = HourViewAdapter(requireActivity(), 10000, actualDateTime)
        hourview_viewpager .adapter = hourViewAdapter
        hourview_viewpager.setCurrentItem(lastViewPosition, false)
    }

    private fun initializeBottomSheet() {
        rosterViewModel.bsBehavior = BottomSheetBehavior.from(mark_unavailable_bs)
        rosterViewModel.UnavailableBS = mark_unavailable_bs

        rosterViewModel.bsBehavior.setPeekHeight(200.px)
        rosterViewModel.bsBehavior.halfExpandedRatio = 0.65F
        rosterViewModel.bsBehavior.isHideable = true

        rosterViewModel.bsBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //TODO("Not yet implemented")
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        Log.d("BS", "Collapsed State")
                        time_collapsed.visibility = View.VISIBLE
                        time_expanded.visibility = View.GONE
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        Log.d("BS", "Expanded State")
                        time_collapsed.visibility = View.GONE
                        time_expanded.visibility = View.VISIBLE
                    }
                    //BottomSheetBehavior.STATE_ANCHOR_POINT -> Log.d("BS", "Anchor Point State")
                    BottomSheetBehavior.STATE_HIDDEN -> Log.d("BS", "Hidden State")
                    BottomSheetBehavior.STATE_DRAGGING -> Log.d("BS", "Dragging State")
                    BottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("BS", "Settling State")
                    }
                    BottomSheetBehavior.STATE_HALF_EXPANDED -> {
                        Log.d("BS", "Half Expanded State")
                        time_collapsed.visibility = View.GONE
                        time_expanded.visibility = View.VISIBLE
                    }
                }
            }

        })
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
        val handler = Handler() { msg ->
            val datetime = LocalDateTime.now()
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


    override fun onDestroy() {
        super.onDestroy()
        hourview_viewpager.unregisterOnPageChangeCallback(hourviewPageChangeCallBack)
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun toggleAvailability() {
        top_bar.isAvailable = !top_bar.isAvailable

        // disable hours
        var hourView: HourRow
        for (idx in 1..24) {
            hourView = day_times.findViewWithTag("hour_$idx")
            hourView.isDisabled = !hourView.isDisabled
        }
        if (!top_bar.isAvailable) {
            if (upcomingGigs.size > 0) {
                showGigsTodayWarning()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showGigsTodayWarning() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.gigs_today_warning_dialog)

        dialog.dialog_content.setText(
            "You have " + upcomingGigs.size.toString() + " Gig(s) active on the day. These gigs will get canceled as well."
        )

        dialog.cancel.setOnClickListener {
            toggleAvailability()
            dialog .dismiss()
        }

        dialog.yes.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on YeS", Toast.LENGTH_SHORT).show()
            showReasonForGigCancel()
            dialog .dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showReasonForGigCancel() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)

        var selectedText = ""

        dialog.cancel_options.setOnCheckedChangeListener ( RadioGroup.OnCheckedChangeListener { group, checkedId ->
            selectedText = dialog.findViewById<RadioButton>(checkedId).text.toString()
        })

        dialog.submit_button.setOnClickListener {
            Toast.makeText(requireContext(), "selected option " + selectedText, Toast.LENGTH_SHORT).show()
            val child = hourview_viewpager.getChildAt(0)

            // removing upcoming gigs
            // TODO: Can take this functioni out
            for (gig in upcomingGigs)
                child.findViewWithTag<ConstraintLayout>("day_times").removeView(child.findViewWithTag<UpcomingGigCard>(gig.tag))
            dialog .dismiss()
        }

        dialog.cancel_button.setOnClickListener {
            toggleAvailability()
            dialog .dismiss()
        }

        dialog.show()
    }

}
