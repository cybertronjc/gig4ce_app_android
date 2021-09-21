package com.gigforce.giger_app.calendarscreen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.gigforce.giger_app.R
import kotlinx.android.synthetic.main.item_gridhomescreen.view.*

class HomeScreenAdapter(
    private val context: Context,
    private val resource: Int,
    private val itemList: ArrayList<HSGridItemDataModel>?
) :
    BaseAdapter() {
    override fun getCount(): Int {
        return if (this.itemList != null) this.itemList.size else 0
    }

    override fun getItem(position: Int): Any {
        return itemList!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var inflator = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var foodView = inflator.inflate(R.layout.item_gridhomescreen, null)
        foodView.textView.text = this.itemList!![position].iconName
        foodView.icon.setImageResource(this.itemList[position].icon)
        return foodView
    }
}
