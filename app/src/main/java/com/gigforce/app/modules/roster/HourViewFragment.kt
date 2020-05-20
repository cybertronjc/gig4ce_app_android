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
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.google.android.material.card.MaterialCardView
import kotlinx.android.synthetic.main.item_roster_day.view.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import kotlinx.android.synthetic.main.roster_day_hour_view.*
import kotlinx.android.synthetic.main.vertical_calendar_item.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class HourViewFragment: RosterBaseFragment() {

    @RequiresApi(Build.VERSION_CODES.O)
    var actualDateTime = LocalDateTime.now()
    lateinit var activeDateTime: LocalDateTime

    val hourIds = ArrayList<Int>()

    @RequiresApi(Build.VERSION_CODES.O)
    var handler = Handler() { msg ->
        var datetime = LocalDateTime.now()
        val marginTop = (itemHeight * datetime.hour + ((datetime.minute / 60.0) * itemHeight).toInt()).px
        val layoutParams = current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(marginCardStart - 8.px, marginTop, 0, 0)
        current_time_divider.requestLayout()
        true
    }

    companion object {
        fun getInstance(position: Int): Fragment {
            val bundle = Bundle()
            bundle.putInt("position", position)
            val hourViewFragment = HourViewFragment()
            hourViewFragment.arguments = bundle
            return hourViewFragment
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    val currentDateTime = LocalDateTime.now()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roster_day_hour_view, inflater, container)
        return getFragmentView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initialize() {
        var position = requireArguments().getInt("position")
        var dayGap = position - 5000
        Log.d("HourView", dayGap.toString())
        if (dayGap < 0) {
            activeDateTime = actualDateTime.minusDays((5000 - position).toLong())
        } else {
            activeDateTime = actualDateTime.plusDays((position - 5000).toLong())
        }

//        Toast.makeText(requireContext(), "Current day ${activeDateTime.toString()}", Toast.LENGTH_SHORT).show()

        if (isSameDate(activeDateTime, actualDateTime)) {
            setCurrentTimeDivider()
            scheduleCurrentTimerUpdate()
            current_time_divider.visibility = View.VISIBLE
        }

        initializeHourViews()
    }

    private fun scheduleCurrentTimerUpdate() {
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initializeHourViews() {
        val times = ArrayList<String>()
        times.addAll(listOf("01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
            "21:00", "22:00", "23:00", "24:00"))

        val timeViewGroup = day_times
        var widget: MaterialCardView
        val constraintSet: ConstraintSet?

        var cardDate = currentDateTime.dayOfMonth
        var cardYear = currentDateTime.year
        var cardMonth = currentDateTime.monthValue - 1

        // Adding hourly widgets
        for ((index, time) in times.withIndex()) {
            widget = HourRow(this.requireContext())
            widget.id = View.generateViewId()
            widget.hour = index + 1
            widget.time = time

            widget.setOnClickListener {
                Toast.makeText(requireContext(), "Clicked on hour " + widget.hour.toString(), Toast.LENGTH_SHORT).show()
            }

            if (isSameDate(activeDateTime, actualDateTime)) {
                if (widget.hour <= currentDateTime.hour) {
                    Log.d("HOURVIEW", "inactive hour")
                    widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
                    widget.isClickable = false
                }
            }

            if (isLessDate(activeDateTime, actualDateTime)) {
                widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
                widget.isClickable = false
            }

//            if (activeDateTime.year <= cardYear &&
//                    (activeDateTime.monthValue - 1) <= cardMonth) {
//                    if(activeDateTime.dayOfMonth == cardDate && activeDateTime.monthValue - 1 == cardMonth) {
//                        if (widget.hour <= currentDateTime.hour) {
//                            Log.d("HOURVIEW", "inactive hour")
//                            widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
//                            widget.isClickable = false
//                        }
//                    } else if {
//                        widget.item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
//                        widget.isClickable = false
//                    }
//                }

            timeViewGroup.addView(widget)

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
    }

    private fun makeHoursInactive() {
        for (idx in hourIds) {
            day_times.findViewById<HourRow>(idx).item_time.setTextColor(resources.getColor(R.color.gray_color_calendar))
            day_times.findViewById<HourRow>(idx).isClickable = false
        }
    }
}