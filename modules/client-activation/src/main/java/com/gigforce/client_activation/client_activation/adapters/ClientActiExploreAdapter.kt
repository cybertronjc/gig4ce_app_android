package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.explore.ClientActiExploreList
import com.gigforce.client_activation.client_activation.explore.OnJobSelectedListener
import com.gigforce.client_activation.client_activation.models.JpExplore
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible


class ClientActiExploreAdapter(
    private val context: Context, private val clientActiExploreList: ClientActiExploreList
) : RecyclerView.Adapter<ClientActiExploreAdapter.ClientActiExploreViewHolder>(),
    Filterable {


    private var originalJobList: List<JpExplore> = emptyList()
    private var filteredJobList: List<JpExplore> = emptyList()

    private val jobsFilter = JobsFilter()

    private var selectedItemIndex: Int = -1
    private var onJobSelectedListener : OnJobSelectedListener? = null

    fun setOnJobSelectedListener(onJobSelectedListener: OnJobSelectedListener){
        this.onJobSelectedListener = onJobSelectedListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ClientActiExploreViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.layout_gig_card_item, parent, false)
        return ClientActiExploreViewHolder(view)
    }

    fun getSelectedItemIndex(): Int {
        return selectedItemIndex
    }

    fun resetSelectedItem() {
        if (selectedItemIndex == -1)
            return

        val tempIndex = selectedItemIndex
        selectedItemIndex = -1
        notifyItemChanged(tempIndex)
    }

    override fun getItemCount(): Int {
        return filteredJobList.size
    }

    override fun onBindViewHolder(holder: ClientActiExploreViewHolder, position: Int) {
        holder.bindValues(filteredJobList.get(position), position)
    }

    fun setData(contacts: List<JpExplore>) {

        this.selectedItemIndex = -1
        this.originalJobList = contacts
        this.filteredJobList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = jobsFilter

    private inner class JobsFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                filteredJobList = originalJobList
            } else {
                val filteredList: MutableList<JpExplore> = mutableListOf()
                for (job in originalJobList) {
                    if (job.title.contains(
                            charString,
                            true
                        )
                    ) filteredList.add(job)
                }
                filteredJobList = filteredList
            }

            val filterResults = FilterResults()
            filterResults.values = filteredJobList
            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredJobList = results?.values as List<JpExplore>
            notifyDataSetChanged()
        }
    }


    inner class ClientActiExploreViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var jobTitleTv: TextView = itemView.findViewById(R.id.gig_title)
        private var jobStatusTv: TextView = itemView.findViewById(R.id.gig_status)
        private var jobImage: ImageView = itemView.findViewById(R.id.card_image)
        private var jobActionTv: TextView = itemView.findViewById(R.id.apply_now)
        private var divider_one: View = itemView.findViewById(R.id.divider_one)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JpExplore, position: Int) {
            jobTitleTv.text = jobProfile.title
            Glide.with(context).load(jobProfile.image).placeholder(ShimmerHelper.getShimmerDrawable()).into(jobImage)

            if (jobProfile.status == "")
                jobStatusTv.gone()
            else{
                jobStatusTv.visible()}
            jobStatusTv.text = if (jobProfile.status == "Interested" || jobProfile.status == "Inprocess") "Pending" else jobProfile.status
            context?.applicationContext?.let {
                jobStatusTv.setTextColor(
                    ContextCompat.getColor(
                        it,
                        if (jobProfile.status == "Interested" || jobProfile.status == "Inprocess") R.color.pending_color else if (jobProfile.status == "Activated" || jobProfile.status == "Submitted") R.color.activated_color else R.color.rejected_color
                    )
                )
            }

            var actionButtonText =
                if (jobProfile.status == "Interested") "Complete Application" else if (jobProfile.status == "Inprocess") "Complete Application"
                else if (jobProfile.status == "") "Apply Now"  else ""
            Log.d("actionText", actionButtonText)
            if (actionButtonText == ""){
                divider_one.invisible()
                jobActionTv.gone()
            Log.d("empty", "true")}
            else{
                divider_one.visible()
                Log.d("empty", "true")
                jobActionTv.visible()
                jobActionTv.text = actionButtonText}

//            when (jobProfile.status){
//
//                "Pending" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_status_pending))
//                                jobActionTv.setText("Complete Application")}
//                "Submitted" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_applied))
//                                jobActionTv.setText("View Application")}
//                "New" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_border_24))
//                            jobActionTv.setText("Apply Now")}
//                "Approved" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_applied))
//                                jobActionTv.setText("Share Gig")}
//                "Applied" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_applied))
//                    jobActionTv.setText("View Application")}
//                "Rejected" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_application_rejected))
//                                jobActionTv.setText("Apply Again")}
//
//            }

            jobActionTv.setOnClickListener {
                clientActiExploreList.takeAction(jobActionTv.text.toString(), jobProfile.profileId, jobProfile.jobProfileTitle)
            }

        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = filteredJobList[newPosition]
            onJobSelectedListener?.onJobSelected(jobProfile)
        }

    }

//    private val shimmer = Shimmer.AlphaHighlightBuilder()// The attributes for a ShimmerDrawable is set by this builder
//        .setDuration(1800) // how long the shimmering animation takes to do one full sweep
//        .setBaseAlpha(0.3f) //the alpha of the underlying children
//        .setHighlightAlpha(0.5f) // the shimmer alpha amount
//        .setDirection(Shimmer.Direction.LEFT_TO_RIGHT)
//        .setAutoStart(true)
//        .build()
//
//    // This is the placeholder for the imageView
//    val shimmerDrawable = ShimmerDrawable().apply {
//        setShimmer(shimmer)
//    }

}