package com.gigforce.app.modules.roster.adapters

import android.content.Context
import android.os.Build
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.roster.CompletedGigCard
import com.gigforce.app.modules.roster.inflate
import com.gigforce.app.modules.roster.px
import kotlinx.android.synthetic.main.completed_gig_card.view.*
import kotlinx.android.synthetic.main.item_roster_day.view.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import java.time.LocalDateTime


@RequiresApi(Build.VERSION_CODES.O)
class TimeAdapter(
    private val times: ArrayList<String>, private var completedGigInfo: ArrayList<String>, private var context: Context, private var view: View
): RecyclerView.Adapter<TimeAdapter.ItemHolder>() {

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

        var found = false

        var completedGigCard: CompletedGigCard? = null

//        if (completedGigInfo.contains(position.toString())) {
//            completedGigCard = CompletedGigCard(context)
//            completedGigCard.gigStartHour = 13
//            completedGigCard.gigDuration = 6.0F
//            completedGigCard.cardHeight = completedGigInfo[2].toInt()
//            completedGigCard.id = View.generateViewId()
//            (view as ConstraintLayout).addView(completedGigCard)
//            found = true
//        }


        if (position == currentHour) {
            Log.d("CURRENT HOUR", position.toString())

            if (found)
                holder.bindItem(item, position, currentMinute, true, view, completedGigCard)
            else
                holder.bindItem(item, position, currentMinute, true)

        }

        else {
            if (found)
                holder.bindItem(item, position)
            else
                holder.bindItem(item, position)
        }

    }

    fun getViewByPosition(position: Int): View? {
        return timeViews[position].view
    }

    class ItemHolder(v: View): RecyclerView.ViewHolder(v), View.OnClickListener {
        var view: View = v

        init {
            v.setOnClickListener(this)
            view = v
        }

        override fun onClick(itemView: View?) {
            Log.d("TimeAdapter", "Item Clicked")
            val context = itemView?.context
        }

        companion object {
            private val time_key = "time"
        }


        fun bindItem(
            item: String, position: Int, currentMinute: Int = 0, dividerVisible: Boolean = false,
            parent_view: View? = null, completedGigCard: CompletedGigCard? = null) {

            view.id = position

            view.item_time.text = item
            if (dividerVisible) {
                view.current_time_divider.visibility = View.VISIBLE
                var p = view.current_time_divider.layoutParams as ViewGroup.MarginLayoutParams
                p.setMargins(85.px, ((currentMinute/60.0) * 48).toInt().px, 0, 0)
                view.current_time_divider.requestLayout()
            }

            parent_view?.let {
                var set = ConstraintSet()
                set.clone(parent_view as ConstraintLayout)
                set.connect(
                    position,
                    ConstraintSet.TOP,
                    completedGigCard!!.id,
                    ConstraintSet.TOP,
                    0
                )
                set.applyTo(parent_view as ConstraintLayout)
                Log.d("TIMER ADAPTER", position.toString())
            }
        }
    }
}

