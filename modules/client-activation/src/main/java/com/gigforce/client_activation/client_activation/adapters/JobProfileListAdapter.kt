package com.gigforce.client_activation.client_activation.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.explore.ApplyNowClickListener
import com.gigforce.client_activation.client_activation.explore.JobProfileCardComponent
import com.gigforce.client_activation.client_activation.explore.JobProfileListFragment
import com.gigforce.client_activation.client_activation.explore.OnJobSelectedListener
import com.gigforce.client_activation.client_activation.models.JpExplore

class JobProfileListAdapter(private val context: Context, private val clientActiExploreList: JobProfileListFragment
) : RecyclerView.Adapter<JobProfileListAdapter.JobProfilesViewHolder>(),
    Filterable {


    private var originalJobList: List<JobProfileDVM> = emptyList()
    private var filteredJobList: List<JobProfileDVM> = emptyList()
    var onItemClick: ((JobProfileDVM) -> Unit)? = null

    private val jobsFilter = JobsFilter()

    private var selectedItemIndex: Int = -1
    private var onJobSelectedListener: OnJobSelectedListener? = null

    fun setOnJobSelectedListener(onJobSelectedListener: OnJobSelectedListener) {
        this.onJobSelectedListener = onJobSelectedListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): JobProfilesViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(R.layout.layout_gig_card_item, parent, false)
        return JobProfilesViewHolder(view)
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

    override fun onBindViewHolder(holder: JobProfilesViewHolder, position: Int) {
        holder.bindValues(filteredJobList.get(position), position)
    }

    fun submitList(contacts: List<JobProfileDVM>) {

        this.selectedItemIndex = -1
        this.originalJobList = contacts
        this.filteredJobList = contacts
        notifyItemRangeRemoved(0, filteredJobList.size)
    }

//    fun updateList(list: List<JobProfileDVM>){
//        val tempList = filteredJobList.toMutableList()
//        tempList.addAll(list)
//        this.submitList(tempList)
////        filteredList = tempList
//
//    }
//
//    fun setData(contacts: List<JobProfileDVM>) {
//
//        this.selectedItemIndex = -1
//        this.originalJobList = contacts
//        this.filteredJobList = contacts
//        notifyDataSetChanged()
//    }

    override fun getFilter(): Filter = jobsFilter

    private inner class JobsFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            if (charString.isEmpty()) {
                val filterResults = FilterResults()
                filterResults.values = originalJobList
                return filterResults
            } else {
                val filteredList: MutableList<JobProfileDVM> = mutableListOf()
                for (job in originalJobList) {
                    if (job.title?.contains(
                            charString,
                            true
                        ) == true
                    ) filteredList.add(job)
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }


        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredJobList = results?.values as List<JobProfileDVM>
//            notifyDataSetChanged()
        }
    }


    inner class JobProfilesViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView){

        private var jobProfileCard: JobProfileCardComponent =
            itemView.findViewById(R.id.jobProfileCard)

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(filteredJobList[adapterPosition])
            }
        }

        fun bindValues(jobProfile: JobProfileDVM, position: Int) {

            jobProfileCard.bindData(jobProfile)

            context?.applicationContext?.let {
                val colorStatus =
                    ContextCompat.getColor(
                        it,
                        if (jobProfile.jp_applicationStatus == "Interested" || jobProfile.jp_applicationStatus == "Inprocess") R.color.pending_color else if (jobProfile.jp_applicationStatus == "Activated" || jobProfile.jp_applicationStatus == "Submitted") R.color.activated_color else R.color.rejected_color
                    )
                jobProfileCard.setStatusColor(colorStatus)
            }

            jobProfileCard.setOnApplyNowClickListener(object : ApplyNowClickListener {
                override fun onApplyNowClicked(v: View, text: String) {
                    clientActiExploreList.takeAction(
                        text,
                        jobProfile.profileId.toString(),
                        jobProfile.title.toString()
                    )
                }
            })
        }

//        override fun onClick(v: View?) {
//            val newPosition = adapterPosition
//            val jobProfile = filteredJobList[newPosition]
//            onJobSelectedListener?.onJobSelected(jobProfile)
//        }
    }
}