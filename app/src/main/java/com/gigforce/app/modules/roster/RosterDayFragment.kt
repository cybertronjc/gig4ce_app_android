package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.app.R
import com.gigforce.app.core.toDate
import com.gigforce.app.modules.custom_gig_preferences.CustomPreferencesViewModel
import com.gigforce.app.modules.custom_gig_preferences.ParamCustPreferViewModel
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.gms.tasks.Tasks.await
import com.riningan.widget.ExtendedBottomSheetBehavior
import kotlinx.android.synthetic.main.day_view_top_bar.*
import kotlinx.android.synthetic.main.day_view_top_bar.view.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.unavailable_time_adjustment_bottom_sheet.*
import kotlinx.android.synthetic.main.vertical_calendar_item.*
import java.time.LocalDateTime
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

    lateinit var hourviewPageChangeCallBack: ViewPager2.OnPageChangeCallback
    lateinit var  viewModelCustomPreference : CustomPreferencesViewModel

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //super.onCreate(savedInstanceState)
        arguments?.let {
            activeDateTime = LocalDateTime.parse(it.getSerializable("active_date").toString())
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModelCustomPreference =
            ViewModelProvider(this, ParamCustPreferViewModel(viewLifecycleOwner)).get(
                CustomPreferencesViewModel::class.java
            )
        //attachHourViewAdapter()
        rosterViewModel.currentDateTime.value = rosterViewModel.currentDateTime.value
        Log.d("RosterDayFragment", rosterViewModel.currentDateTime.value.toString())
//        rosterViewModel.currentDateTime.value = rosterViewModel.currentDateTime.value

        return inflateView(R.layout.roster_day_fragment, inflater, container)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //observer()
        initialize()
        setListeners()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        // initialize view model properties
        rosterViewModel.topBar = top_bar
        rosterViewModel.nestedScrollView = nested_scroll_view
        rosterViewModel.currentDateTime.value = activeDateTime
       // rosterViewModel.scrollToPosition(activeDateTime.toDate)

        // set custom preference variable
        setCustomPreference()
//
//        dayTag = rosterViewModel.getTagFromDate(activeDateTime.toDate)
//        if (dayTag !in rosterViewModel.allGigs.keys)
//            rosterViewModel.allGigs.put(dayTag, MutableLiveData(ArrayList<Gig>()))
//        rosterViewModel.getGigs(activeDateTime.toDate)

        //observer()
        initializeBottomSheet()
        attachHourViewAdapter()
        attachDayAvailabilityObserver()
        attachCurrentDateTimeChangeObserver()
        attachTopBarMonthChangeListener()
        attachTopBarMenu()

        rosterViewModel.currentDateTime.observe(viewLifecycleOwner, Observer {

            getDayTimesChild()?.let {
                    rosterViewModel.resetDayTimeAvailability(
                        viewModelCustomPreference,
                        getDayTimesChild()!!
                    )
            }

            rosterViewModel.scrollToPosition(it.toDate)
        })

        viewModelCustomPreference.customPreferencesLiveDataModel.observe(
            viewLifecycleOwner, Observer {
                getDayTimesChild()?.let {
                    rosterViewModel.resetDayTimeAvailability(
                        viewModelCustomPreference,
                        getDayTimesChild()!!
                    )
                }
            }
        )

    }

    private fun attachTopBarMenu() {
        top_bar.more_bottom.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(requireContext(), more_bottom)
            popupMenu.menuInflater.inflate(R.menu.roster_menu, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener {
                when(it.itemId) {
                    R.id.location_card -> {

                    }
                    R.id.settings -> {

                    }
                    R.id.help -> {

                    }
                }
                true
            }
            popupMenu.show()
        }
    }

    private fun getDayTimesChild(): ConstraintLayout? {
        return hourview_viewpager.getChildAt(0).findViewWithTag("day_times")
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
                val newDateTime =  (
                        if (position < lastViewPosition) activeDateTime.minusDays(1)
                        else activeDateTime.plusDays(1)
                        )
                rosterViewModel.currentDateTime.value = newDateTime
                lastViewPosition = position
            }
        }

        hourview_viewpager.registerOnPageChangeCallback(hourviewPageChangeCallBack)

        top_bar.available_toggle.setOnClickListener {
            rosterViewModel.switchDayAvailability(
                requireContext(), getDayTimesChild()!!,
                rosterViewModel.isDayAvailable.value!!, viewModelCustomPreference)

            rosterViewModel.resetDayTimeAvailability(
                viewModelCustomPreference, getDayTimesChild()!!)
        }
    }

    private fun setCustomPreference() {
        try {
            viewModelCustomPreference.customPreferencesDataModel
        }catch (e:UninitializedPropertyAccessException){
            viewModelCustomPreference.getAllData()
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

                if (monthGap == 0)
                    return

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

        rosterViewModel.currentDateTime.observe( viewLifecycleOwner, Observer { it ->
            activeDateTime = it
            top_bar.year = it.year
            top_bar.month = it.monthValue - 1
            top_bar.date = it.dayOfMonth
            top_bar.day = it.dayOfWeek.value - 1

            top_bar.isCurrentDay = isSameDate(it, actualDateTime)
            top_bar.isFutureDate = isMoreDate(it, actualDateTime)

            top_bar.toggleInactive = isLessDate(it, actualDateTime)

            //dayTag = "${activeDateTime.year}${activeDateTime.monthValue}${activeDateTime.dayOfMonth}"
            dayTag = String.format("%4d", activeDateTime.year) +
                    String.format("%02d", activeDateTime.monthValue) +
                    String.format("%02d", activeDateTime.dayOfMonth)

        })
    }

    private fun attachHourViewAdapter() {
        val hourViewAdapter = HourViewAdapter(requireActivity(), 10000, activeDateTime)
        hourview_viewpager .adapter = hourViewAdapter
        hourview_viewpager.setCurrentItem(lastViewPosition, false)
    }

    private fun initializeBottomSheet() {
        rosterViewModel.bsBehavior = ExtendedBottomSheetBehavior.from(mark_unavailable_bs)
        rosterViewModel.UnavailableBS = mark_unavailable_bs

        rosterViewModel.bsBehavior.isHideable = true
        rosterViewModel.bsBehavior.state = ExtendedBottomSheetBehavior.STATE_HIDDEN

        rosterViewModel.bsBehavior.setBottomSheetCallback(object: ExtendedBottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                //TODO("Not yet implemented")
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                when(newState) {
                    ExtendedBottomSheetBehavior.STATE_COLLAPSED -> {
                        //showToast("Collapsed State")
                        time_expanded.visibility = View.GONE
                        time_collapsed.visibility = View.VISIBLE
                        //rosterViewModel.bsBehavior.state = ExtendedBottomSheetBehavior.STATE_COLLAPSED
                    }
                    ExtendedBottomSheetBehavior.STATE_EXPANDED -> {
                        //showToast("expanded state")
                        time_collapsed.visibility = View.GONE
                        time_expanded.visibility = View.VISIBLE
                    }
                    ExtendedBottomSheetBehavior.STATE_HALF -> {
                        time_collapsed.visibility = View.GONE
                        time_expanded.visibility = View.VISIBLE
                    }
                    //BottomSheetBehavior.STATE_ANCHOR_POINT -> Log.d("BS", "Anchor Point State")
                    ExtendedBottomSheetBehavior.STATE_HIDDEN -> Log.d("BS", "Hidden State")
                    ExtendedBottomSheetBehavior.STATE_DRAGGING -> Log.d("BS", "Dragging State")
                    ExtendedBottomSheetBehavior.STATE_SETTLING -> {
                        Log.d("BS", "Settling State")
                    }
                }
            }

        })
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

}
