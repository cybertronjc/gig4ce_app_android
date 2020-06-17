package com.gigforce.app.modules.wallet

import android.graphics.PorterDuff
import android.graphics.drawable.VectorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.gigforce.app.R
import com.gigforce.app.modules.calendarscreen.HomeScreenFragment
import com.github.vipulasri.timelineview.TimelineView
import kotlinx.android.synthetic.main.item_timeline.view.*
import kotlinx.android.synthetic.main.payment_dispute_expanded_page.*

class PaymentDisputeExpandedPage: WalletBaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.payment_dispute_expanded_page, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timelineview.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = TimelineAdapter(ArrayList((1..10).toList()))
        }

        back_button.setOnClickListener { requireActivity().onBackPressed() }
    }
}

class TimelineAdapter(
    private val items: ArrayList<Int>): RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    private lateinit var layoutInflater: LayoutInflater

    override fun getItemViewType(position: Int): Int {
        return TimelineView.getTimeLineViewType(position, itemCount)
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TimelineAdapter.TimelineViewHolder {
        if(!::layoutInflater.isInitialized) {
            layoutInflater = LayoutInflater.from(parent.context)
        }
        return TimelineViewHolder(
            layoutInflater.inflate(R.layout.item_timeline, parent, false), viewType)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: TimelineAdapter.TimelineViewHolder, position: Int) {
        if (position == items.size - 1) {
            //setMarker(holder, R.drawable.ic_filled_circle, R.color.colorPrimary)
            holder.timeline.marker = holder.itemView.context.resources.getDrawable(R.drawable.ic_filled_circle)
        } else {
            //setMarker(holder, R.drawable.ic_circle_empty, R.color.colorPrimary)

            holder.timeline.marker = holder.itemView.context.resources.getDrawable(R.drawable.ic_circle_empty)
        }
    }

    private fun setMarker(holder: TimelineAdapter.TimelineViewHolder, drawableId: Int, colorFilter: Int) {
        //holder.timeline.marker =
        var drawable = VectorDrawableCompat.create(holder.itemView.context.resources, drawableId, holder.itemView.context.theme)
        drawable!!.setColorFilter(colorFilter, PorterDuff.Mode.SRC_IN)
        drawable!!.setTint(holder.itemView.context.resources.getColor(R.color.colorPrimary))
        holder.timeline.marker = drawable
    }

    class TimelineViewHolder(itemView: View, viewType: Int): RecyclerView.ViewHolder(itemView) {

        val timeline = itemView.timeline

        init {
            itemView.timeline.initLine(viewType)
        }
    }

}