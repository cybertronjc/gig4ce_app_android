package com.gigforce.app.core.base.components

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.gigforce.app.R

class CalendarBaseAdapter : BaseAdapter (){

    lateinit var list : ArrayList<CalendarView.DayModel>

    override fun getView(position:Int, convertView: View?, parent: ViewGroup?):View{
        val inflater = parent?.context?.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.calendar_view_layout_item,null)
        val tv = view.findViewById<TextView>(R.id.calendar_date)
        tv.text = list[position].date.toString()
        if(list[position].currentMonth!=list[position].month)
        tv.setTextColor(parent?.context.getColor(R.color.different_month_calendar_color))
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

    fun setCalendarData(list : ArrayList<CalendarView.DayModel>){
        this.list = list
    }

}