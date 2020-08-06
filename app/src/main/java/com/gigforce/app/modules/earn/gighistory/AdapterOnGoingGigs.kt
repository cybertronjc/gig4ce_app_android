package com.gigforce.app.modules.earn.gighistory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.PushDownAnim
import kotlinx.android.synthetic.main.layout_rv_gig_details_gig_history.view.*
import java.text.SimpleDateFormat

class AdapterOnGoingGigs : RecyclerView.Adapter<AdapterOnGoingGigs.ViewHolder>() {
    private lateinit var callbacks: AdapterOnGoingGigCallbacks
    private var onGoingGigs: List<Gig>? = null
    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.layout_rv_gig_details_gig_history, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (onGoingGigs != null) onGoingGigs!!.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val gig = onGoingGigs?.get(position)

        holder.itemView.tv_designation_rv_gig_hist.text = gig?.title
        holder.itemView.tv_gig_venue_rv_gig_his.text = "@${gig?.companyName}"
        holder.itemView.tv_gig_venue_rv_gig_his.isSelected = true
        holder.itemView.tv_rating_rv_gig_hist.text = gig?.gigRating.toString()
        holder.itemView.tv_punch_in_time_rv_gig_hist.text = "--:--"
        holder.itemView.tv_punch_out_time_rv_gig_hist.text = "--:--"
        gig?.attendance?.checkInTime?.let {
            holder.itemView.tv_punch_in_time_rv_gig_hist.text = timeFormatter.format(it)
        }
        gig?.attendance?.checkOutTime?.let {
            holder.itemView.tv_punch_out_time_rv_gig_hist.text = timeFormatter.format(it)
        }
        Glide.with(holder.itemView).load(gig?.companyLogo).placeholder(R.drawable.profile)
            .into(holder.itemView.iv_brand_rv_gig_hist)
        PushDownAnim.setPushDownAnimTo(holder.itemView).setOnClickListener(View.OnClickListener {
            callbacks.openGigDetails(onGoingGigs!![holder.adapterPosition])
        })

    }

    fun setCallbacks(callbacks: AdapterOnGoingGigCallbacks) {
        this.callbacks = callbacks
    }

    fun addData(onGoingGigs: List<Gig>?) {
        this.onGoingGigs = onGoingGigs;
        notifyDataSetChanged()
    }

    interface AdapterOnGoingGigCallbacks {
        fun openGigDetails(gig: Gig)
    }
}