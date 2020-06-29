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
        // Inflate the custom view
        val inflater = parent?.context?.
            getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.calendar_view_layout_item,null)

        // Get the custom view widgets reference
        val tv = view.findViewById<TextView>(R.id.calendar_date)
        val card = view.findViewById<CardView>(R.id.card_view)

        // Display color name on text view
        tv.text = list[position].date.toString()

        // Set background color for card view
//        card.setCardBackgroundColor(list[position].status)

        // Set a click listener for card view
//        card.setOnClickListener{
//        }

        // Finally, return the view
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