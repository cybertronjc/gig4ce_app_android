package com.gigforce.app.utils.widgets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class TempFragment : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.calendar_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    val dateFormat = SimpleDateFormat("MMMM YYYY")
    var calendar = Calendar.getInstance()
    fun listener() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        calendarView.setMonthChangeListener(object :
                CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
//                showToast(""+monthModel.currentMonth +" "+ monthModel.year)
                var selectedYear = monthModel.year
//                if(monthModel.currentMonth == 11)
//                    selectedYear = 2019
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, monthModel.currentMonth)
                month_year.text = dateFormat.format(calendar.time)
            }

        })
        calendarView.setOnDateClickListner(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                showToast("" + monthModel.currentMonth + " " + monthModel.year)
            }
        })
        calendarView.setGigData(ArrayList<AllotedGigDataModel>())
        next_year.setOnClickListener() {
            var currentVisiblePosition =
                    (calendarView.findViewById<RecyclerView>(R.id.calendar_rv).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            (calendarView.findViewById<RecyclerView>(R.id.calendar_rv)).smoothScrollToPosition(currentVisiblePosition + 1)
        }
        previous_year.setOnClickListener() {
            var currentVisiblePosition =
                    (calendarView.findViewById<RecyclerView>(R.id.calendar_rv).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            (calendarView.findViewById<RecyclerView>(R.id.calendar_rv)).smoothScrollToPosition(currentVisiblePosition - 1)

        }
    }

}