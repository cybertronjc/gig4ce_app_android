package com.gigforce.app.modules.earn

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.utils.HorizontaltemDecoration
import kotlinx.android.synthetic.main.layout_rv_gig_details_gig_history.view.*
import kotlinx.android.synthetic.main.layout_rv_ongoing_gigs_gig_hist.view.*

class AdapterGigHistory : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    inner class ViewHolderOnGoingGigs(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ViewHolderGigEvents(itemView: View) : RecyclerView.ViewHolder(itemView)
    inner class ViewHolderGigDetails(itemView: View) : RecyclerView.ViewHolder(itemView)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_ONGOING -> ViewHolderOnGoingGigs(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_ongoing_gigs_gig_hist, parent, false)
            )
            TYPE_EVENTS -> ViewHolderGigEvents(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_gig_events_gig_hist, parent, false)
            )
            else -> ViewHolderGigDetails(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.layout_rv_gig_details_gig_history, parent, false)
            )
        }

    }

    override fun getItemCount(): Int {
        return 12;
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            TYPE_ONGOING -> {
                val viewHolderOnGoings = holder as ViewHolderOnGoingGigs
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.adapter = AdapterOnGoingGigs()
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.addItemDecoration(
                    HorizontaltemDecoration(holder.itemView.resources.getDimensionPixelOffset(R.dimen.size_8))
                )
                viewHolderOnGoings.itemView.rv_on_going_gigs_gig_hist.layoutManager =
                    LinearLayoutManager(
                        holder.itemView.context,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
            }
            TYPE_EVENTS -> {

            }
            else -> {
                val viewHolderGigDetails = holder as ViewHolderGigDetails
                if (position == 2) {
                    viewHolderGigDetails.itemView.tv_gig_day_rv_gig_his.visibility = View.VISIBLE
                } else {
                    viewHolderGigDetails.itemView.tv_gig_day_rv_gig_his.visibility = View.GONE
                }
                viewHolderGigDetails.itemView.tv_date_gig_hist.isSelected=true
                viewHolderGigDetails.itemView.rl_on_going_gig_hist.visibility = View.GONE
                viewHolderGigDetails.itemView.rl_scheduled_gig_hist.visibility = View.VISIBLE

            }
        }
    }

    companion object {
        const val TYPE_ONGOING = 1
        const val TYPE_EVENTS = 2
        const val TYPE_GIG_DETAILS = 3


    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> TYPE_ONGOING
            1 -> TYPE_EVENTS
            else -> TYPE_GIG_DETAILS
        }
    }
}