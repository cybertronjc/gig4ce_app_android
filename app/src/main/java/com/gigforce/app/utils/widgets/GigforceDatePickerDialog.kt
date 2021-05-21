package com.gigforce.app.utils.widgets

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.components.CalendarView
import com.gigforce.giger_app.calendarscreen.maincalendarscreen.verticalcalendar.AllotedGigDataModel
import com.gigforce.common_ui.utils.getScreenWidth
import kotlinx.android.synthetic.main.calendar_dialog.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class GigforceDatePickerDialog : DialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.calendar_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                getScreenWidth(
                    requireActivity()
                ).width - resources.getDimensionPixelSize(R.dimen.size_4), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private lateinit var callbacks: GigforceDatePickerDialogCallbacks
    val dateFormat = SimpleDateFormat("MMMM yyyy")

    var calendar = Calendar.getInstance()
    fun listener() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"))
        month_year.text = dateFormat.format(calendar.time)
        calendarView.setMonthChangeListener(object :
                CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
//                showToast(""+monthModel.currentMonth +" "+ monthModel.year)
                var selectedYear = monthModel.year
//                if(monthModel.currentMonth == 11)
//                    selectedYear = 2019
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.DATE, monthModel.days[monthModel.days.size / 2].date)
                calendar.set(Calendar.YEAR, selectedYear)
                calendar.set(Calendar.MONTH, monthModel.currentMonth)
                month_year.text = dateFormat.format(calendar.time)
            }

        })
        calendarView.setOnDateClickListner(object : CalendarView.MonthChangeAndDateClickedListener {
            override fun onMonthChange(monthModel: CalendarView.MonthModel) {
                callbacks.selectedDate(monthModel.days[monthModel.days.size / 2].date.toString() + "/" + (monthModel.currentMonth + 1) + "/" + monthModel.year)
                dismiss()
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

    fun setCallbacks(callbacks: GigforceDatePickerDialogCallbacks) {
        this.callbacks = callbacks
    }

    public interface GigforceDatePickerDialogCallbacks {

        fun selectedDate(monthModel: String)
    }

}