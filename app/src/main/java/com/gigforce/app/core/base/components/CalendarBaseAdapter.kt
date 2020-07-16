package com.gigforce.app.core.base.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.gigforce.app.R
import com.gigforce.app.core.visible
import java.util.*
import kotlin.collections.ArrayList

class CalendarBaseAdapter(var context: Context) : BaseAdapter() {

    lateinit var list: ArrayList<CalendarView.DayModel>
    var todayDate = Calendar.getInstance()
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater =
            parent?.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.calendar_view_layout_item, null)
        val tv = view.findViewById<TextView>(R.id.calendar_date)
        val gigIndicator1 = view.findViewById<View>(R.id.gig_indicator1)
        val gigIndicator2 = view.findViewById<View>(R.id.gig_indicator2)
        val gigIndicator3 = view.findViewById<View>(R.id.gig_indicator3)

        tv.text = list[position].date.toString()
        if (list[position].currentMonth != list[position].month)
            tv.setTextColor(parent?.context.getColor(R.color.different_month_calendar_color))
        if (todayDate.get(Calendar.DATE) == list[position].date && todayDate.get(Calendar.MONTH) == list[position].month && todayDate.get(
                Calendar.YEAR
            ) == list[position].year
        ) {
            var dateTV = view.findViewById<TextView>(R.id.calendar_date)
            dateTV.background = context.getDrawable(R.drawable.current_date_indicator)
            dateTV.setTextColor(context.getColor(R.color.white))

        }
        if (list[position].gigData != null) {
            if(list[position].gigData?.size!!>0) {
                gigIndicator1.visible()
            }
            if(list[position].gigData?.size!!>1) {
                gigIndicator2.visible()
            }
            if(list[position].gigData?.size!!>2) {
                gigIndicator3.visible()
            }
        }
        view.setOnClickListener {
            var monthModel = CalendarView.MonthModel()
            monthModel.year = list[position].year
            monthModel.currentMonth = list[position].month
            monthModel.days.add(
                CalendarView.DayModel(
                    list[position].date,
                    list[position].month,
                    list[position].year
                )
            )
            if (this::dateClickListener.isInitialized) {
                dateClickListener.onMonthChange(monthModel)
            }
        }
        return view
    }


    override fun getItem(position: Int): Any? {
        return list[position]
    }


    override fun getItemId(position: Int): Long {
        return position.toLong()
    }


    // Count the items
    override fun getCount(): Int {
        return list.size
    }

    fun setCalendarData(list: ArrayList<CalendarView.DayModel>) {
        this.list = list
    }

    lateinit var dateClickListener: CalendarView.MonthChangeAndDateClickedListener
    fun setDateClickedListener(dateClickListener: CalendarView.MonthChangeAndDateClickedListener) {
        this.dateClickListener = dateClickListener
    }

}