package com.gigforce.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.base.components.CalendarView
import com.gigforce.app.modules.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import com.gigforce.common_ui.ext.showToast
import kotlinx.android.synthetic.main.calendar_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TempFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.calendar_dialog, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }
    val dateFormat = SimpleDateFormat("MMMM YYYY")
    var calendar = Calendar.getInstance()
    fun listener(){
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        calendarView.setMonthChangeListener(object :
            CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
//                showToast(""+monthModel.currentMonth +" "+ monthModel.year)
                var selectedYear = monthModel.year
//                if(monthModel.currentMonth == 11)
//                    selectedYear = 2019
                calendar.set(Calendar.YEAR,selectedYear)
                calendar.set(Calendar.MONTH,monthModel.currentMonth)
                month_year.text = dateFormat.format(calendar.time)
            }

        })
        calendarView.setOnDateClickListner(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                showToast(""+monthModel.currentMonth +" "+ monthModel.year)
            }
        })
        calendarView.setGigData(ArrayList<AllotedGigDataModel>())
        next_year.setOnClickListener(){
            var currentVisiblePosition =
                (calendarView.findViewById<RecyclerView>(R.id.calendar_rv).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            (calendarView.findViewById<RecyclerView>(R.id.calendar_rv)).smoothScrollToPosition(currentVisiblePosition+1)
        }
        previous_year.setOnClickListener(){
            var currentVisiblePosition =
                (calendarView.findViewById<RecyclerView>(R.id.calendar_rv).layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
            (calendarView.findViewById<RecyclerView>(R.id.calendar_rv)).smoothScrollToPosition(currentVisiblePosition-1)

        }
    }

}