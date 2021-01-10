package com.gigforce.app.modules.earn.gighistory

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.modules.gigPage.models.Gig
import com.gigforce.app.utils.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.layout_rv_gig_details_gig_history.view.*
import java.text.SimpleDateFormat

class AdapterOnGoingGigs : RecyclerView.Adapter<AdapterOnGoingGigs.ViewHolder>() {
    private var callbacks: AdapterOnGoingGigCallbacks? = null
    private var onGoingGigs: MutableList<Gig>? = ArrayList<Gig>()
    private val timeFormatter = SimpleDateFormat("hh.mm aa")

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val resources = itemView.context.resources
        val size16 = resources.getDimensionPixelSize(R.dimen.size_16)
        val size32 = resources.getDimensionPixelSize(R.dimen.size_32)
        val size24 = resources.getDimensionPixelSize(R.dimen.size_28)

        val size4 = resources.getDimensionPixelSize(R.dimen.size_4)
        val size8 = resources.getDimensionPixelSize(R.dimen.size_8)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
                LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_rv_on_going_gigs, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return if (onGoingGigs != null) onGoingGigs!!.size else 0
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val params: RecyclerView.LayoutParams =
                holder.itemView.cl_ongoing_gigs.layoutParams as RecyclerView.LayoutParams

        params.topMargin = holder.size4;
        params.leftMargin = 0
        params.rightMargin = 0
        params.bottomMargin = holder.size16
        params.width = getScreenWidth(holder.itemView.context as Activity).width - holder.size24
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        holder.itemView.cl_ongoing_gigs.layoutParams = params

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
        setBrandLogo(gig ?: Gig(), holder)
        PushDownAnim.setPushDownAnimTo(holder.itemView)
                .setOnClickListener(View.OnClickListener {
                    callbacks?.openGigDetails(onGoingGigs!![holder.adapterPosition])
                })


    }

    fun setCallbacks(callbacks: AdapterOnGoingGigCallbacks) {
        this.callbacks = callbacks
    }

    fun addData(onGoingGigs: List<Gig>?) {
        this.onGoingGigs?.clear()
        this.onGoingGigs?.addAll(onGoingGigs!!)
        notifyDataSetChanged()
    }

    interface AdapterOnGoingGigCallbacks {
        fun openGigDetails(gig: Gig)
    }

    private fun setBrandLogo(
            gig: Gig,
            viewHolderGigDetails: ViewHolder
    ) {
        if (!gig.companyLogo.isNullOrBlank()) {
            if (gig.companyLogo!!.startsWith("http", true)) {

                GlideApp.with(viewHolderGigDetails.itemView.context)
                        .load(gig.companyLogo)
                        .placeholder(getCircularProgressDrawable(viewHolderGigDetails.itemView.context))
                        .into(viewHolderGigDetails.itemView.iv_brand_rv_gig_hist)
            } else {
                FirebaseStorage.getInstance()
                        .getReference("companies_gigs_images")
                        .child(gig.companyLogo!!)
                        .downloadUrl
                        .addOnSuccessListener { fileUri ->

                            GlideApp.with(viewHolderGigDetails.itemView)
                                    .load(fileUri)
                                    .placeholder(getCircularProgressDrawable(viewHolderGigDetails.itemView.context))
                                    .into(viewHolderGigDetails.itemView.iv_brand_rv_gig_hist)
                        }
            }
        } else {
            val companyInitials = if (gig.companyName.isNullOrBlank())
                "C"
            else
                gig.companyName!![0].toString().toUpperCase()
            val drawable = TextDrawable.builder().buildRound(
                    companyInitials,
                    ResourcesCompat.getColor(
                            viewHolderGigDetails.itemView.context.resources,
                            R.color.lipstick,
                            null
                    )
            )

            viewHolderGigDetails.itemView.iv_brand_rv_gig_hist.setImageDrawable(drawable)
        }
    }

    fun removeItem(itemToBeRemoved: Int) {
        onGoingGigs?.removeAt(itemToBeRemoved)
        notifyItemRemoved(itemToBeRemoved)

    }

    fun itemAdded(itemToBeRemoved: Int, gig: Gig) {
        onGoingGigs?.add(itemToBeRemoved, gig)
        notifyItemInserted(itemToBeRemoved)
    }

    fun itemModified(index: Int, gig: Gig) {
        onGoingGigs!![index] = gig
        notifyItemChanged(index)
    }

}