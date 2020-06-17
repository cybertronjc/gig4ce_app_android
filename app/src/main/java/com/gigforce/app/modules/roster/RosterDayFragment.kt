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
import kotlinx.android.synthetic.main.day_view_top_bar.*
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

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("RDF", "Coming in oncreate with arguments " + arguments.toString())
        arguments?.let {
            activeDateTime = LocalDateTime.parse(it.getSerializable("active_date").toString())
            Log.d("RDF", activeDateTime.toString())
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roster_day_fragment, inflater, container)

        rosterViewModel.currentDateTime.value = activeDateTime

        return getFragmentView()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        initialize()
        setListeners()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        rosterViewModel.topBar = top_bar
        initializeBottomSheet()
        attachHourViewAdapter()
        attachDayAvailabilityObserver()
        attachCurrentDateTimeChangeObserver()
        attachTopBarMonthChangeListener()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setListeners() {
        back_button.setOnClickListener{
            activity?.onBackPressed()
        }
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
        }

        hourview_viewpager.registerOnPageChangeCallback(hourviewPageChangeCallBack)



        top_bar.available_toggle.setOnClickListener {
            if (top_bar.isAvailable)
                toggleAvailability()
            else {
                rosterViewModel.isDayAvailable.value = true
                setHourVisibility(hourview_viewpager.getChildAt(0).findViewWithTag<ConstraintLayout>("day_times"), activeDateTime, actualDateTime)
            }
        }
    }

    private fun attachDayAvailabilityObserver() {
        rosterViewModel.isDayAvailable.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
           top_bar.isAvailable = it
        })
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

            dayTag = "${activeDateTime.year}${activeDateTime.monthValue}${activeDateTime.dayOfMonth}"

//            upcomingGigs.clear()
//            upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag))
            rosterViewModel.gigsQuery.value ?.let {
                upcomingGigs.clear()
                upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag, it))
                completedGigs.clear()
                completedGigs.addAll(rosterViewModel.getCompletedGigsByDayTag(dayTag, it))
            }

        })
        rosterViewModel.gigsQuery.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            upcomingGigs.clear()
            upcomingGigs.addAll(rosterViewModel.getUpcomingGigsByDayTag(dayTag, it))

            completedGigs.clear()
            completedGigs.addAll(rosterViewModel.getCompletedGigsByDayTag(dayTag, it))
        })
    }

    private fun attachHourViewAdapter() {
        val hourViewAdapter = HourViewAdapter(requireActivity(), 10000, activeDateTime)
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
                        //time_expanded.visibility = View.GONE
                        //time_collapsed.visibility = View.VISIBLE
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
//                        time_collapsed.visibility = View.GONE
//                        time_expanded.visibility = View.VISIBLE
                    }
                }
            }

        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
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
    fun toggleAvailability() {
       if (upcomingGigs.size > 0) {
           rosterViewModel.isDayAvailable.value = !showGigsTodayWarning(
               requireContext(), upcomingGigs,
               hourview_viewpager.getChildAt(0).findViewWithTag<ConstraintLayout>("day_times"))
        } else {
           rosterViewModel.isDayAvailable.value = false
           allHourInactive(
               hourview_viewpager.getChildAt(0).findViewWithTag<ConstraintLayout>("day_times"))
        }
    }

}
