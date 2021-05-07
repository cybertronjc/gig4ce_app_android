package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.explore.ClientActiExploreList
import com.gigforce.client_activation.client_activation.explore.OnJobSelectedListener
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.client_activation.client_activation.models.JpExplore


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
        private var jobStatusIcon: ImageView = itemView.findViewById(R.id.status_icon)
        private var jobActionTv: TextView = itemView.findViewById(R.id.apply_now)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JpExplore, position: Int) {
            jobTitleTv.text = jobProfile.title
            jobStatusTv.text = jobProfile.status
            Glide.with(context).load(jobProfile.image).into(jobImage)

            when (jobProfile.status){

                "Pending" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_status_pending))
                                jobActionTv.setText("Complete Application")}
                "Submitted" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_status_pending))
                                jobActionTv.setText("View Application")}
                "New" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_border_24))
                            jobActionTv.setText("Apply Now")}
                "Approved" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_border_24))
                                jobActionTv.setText("Share Gig")}
                "Applied" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_applied))
                    jobActionTv.setText("View Application")}
                "Rejected" -> { jobStatusIcon.setImageDrawable(context.getDrawable(R.drawable.ic_baseline_star_border_24))
                                jobActionTv.setText("Apply Again")}

            }

            jobActionTv.setOnClickListener {
                clientActiExploreList.takeAction(jobActionTv.text.toString(), jobProfile.profileId)
            }

        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition
            val jobProfile = filteredJobList[newPosition]
            onJobSelectedListener?.onJobSelected(jobProfile)
        }

    }

}