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
import com.gigforce.client_activation.client_activation.explore.OnJobSelectedListener
import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.core.utils.GlideApp

class ClientActiExploreAdapter(
    private val context: Context
) : RecyclerView.Adapter<ClientActiExploreAdapter.ClientActiExploreViewHolder>(),
    Filterable {


    private var originalJobList: List<JobProfile> = emptyList()
    private var filteredJobList: List<JobProfile> = emptyList()

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

    fun setData(contacts: List<JobProfile>) {

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
                val filteredList: MutableList<JobProfile> = mutableListOf()
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
            filteredJobList = results?.values as List<JobProfile>
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


        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JobProfile, position: Int) {
            jobTitleTv.text = jobProfile.cardTitle
            //jobStatusTv.text = jobProfile.subTitle
            GlideApp.with(context).load(jobProfile.cardImage).into(jobImage)

        }

        override fun onClick(v: View?) {
            val newPosition = adapterPosition

            if (selectedItemIndex != -1) {
                val tempIndex = selectedItemIndex
                selectedItemIndex = newPosition
                notifyItemChanged(tempIndex)
                notifyItemChanged(selectedItemIndex)
            } else {
                selectedItemIndex = newPosition
                notifyItemChanged(selectedItemIndex)
            }

            val jobProfile = filteredJobList[newPosition]
            onJobSelectedListener?.onJobSelected(jobProfile)
        }

    }

}