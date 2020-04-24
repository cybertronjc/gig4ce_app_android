package com.gigforce.app.modules.roster.adapters

import android.content.res.Resources
import android.os.Build
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.modules.roster.px
import kotlinx.android.synthetic.main.item_roster_day.view.*
import java.security.AccessController.getContext
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
class TimeAdapter(private val times: ArrayList<String>): RecyclerView.Adapter<TimeAdapter.ItemHolder>() {
    var timeViews = ArrayList<ItemHolder>()
    var currentHour = 0
    var currentMinute = 0

    init {
        var datetime = LocalDateTime.now()
        currentHour = datetime.hour
        currentMinute = datetime.minute
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeAdapter.ItemHolder {
        val inflatedView = parent.inflate(R.layout.item_roster_day, false)
        return ItemHolder(inflatedView)
    }

    override fun getItemCount(): Int {
        return times.size
    }

    override fun onBindViewHolder(holder: TimeAdapter.ItemHolder, position: Int) {
        val item = times[position]
        timeViews.add(holder)
        if (position == currentHour)
            holder.bindItem(item, currentMinute, true)
        else
            holder.bindItem(item)
    }

    fun getViewByPosition(position: Int): View? {
        return timeViews[position].view
    }

    class ItemHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
        var view: View = v

        init {
            v.setOnClickListener(this)
        }

        override fun onClick(itemView: View?) {
            Log.d("TimeAdapter", "Item Clicked")
            val context = itemView?.context
        }

        companion object {
            private val time_key = "time"
        }

        fun bindItem(item: String, currentMinute: Int = 0, dividerVisible: Boolean = false) {
            view.item_time.text = item
            if (dividerVisible) {

                view.current_time_divider.visibility = View.VISIBLE
                var p = view.current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(85.px, ((currentMinute/60.0) * 48).toInt().px, 0, 0)
                view.current_time_divider.requestLayout()
            }
        }
    }
}

