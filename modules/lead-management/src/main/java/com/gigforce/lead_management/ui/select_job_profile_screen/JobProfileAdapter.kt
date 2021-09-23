package com.gigforce.lead_management.ui.select_job_profile_screen

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.lead_management.R

class JobProfileAdapter(
        private val context: Context,
        private val requestManager: RequestManager
) : RecyclerView.Adapter<JobProfileAdapter.BusinessViewHolder>(),
        Filterable {

    private var originalProfileList= ArrayList<JobProfilesItem>()
    private var filteredProfileList= ArrayList<JobProfilesItem>()

    private val contactsFilter = CityFilter()

    private var selectedId: String? = null
    //    private var selectedItemIndex: Int = -1

    private var onJobProfileSelectedListener: OnJobProfileSelectedListener? = null

    fun setOnJobProfileSelectedListener(onJobProfileSelectedListener: OnJobProfileSelectedListener) {
        this.onJobProfileSelectedListener = onJobProfileSelectedListener
    }

    override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
    ): BusinessViewHolder {
        val view = LayoutInflater.from(
                parent.context
        ).inflate(
            R.layout.recycler_item_job_profile,
            parent,
            false
        )
        return BusinessViewHolder(view)
    }

    fun getSelectedBusiness() : JobProfilesItem?{
        if (selectedId == null)
            return null
        else {
            return filteredProfileList[getIndexFromId(selectedId!!)]
        }
    }


    override fun getItemCount(): Int {
        return filteredProfileList.size
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        if(position != RecyclerView.NO_POSITION
                && position < filteredProfileList.size
        ) {
            holder.bindValues(filteredProfileList.get(position), position)
        }
    }

    fun setData(contacts: ArrayList<JobProfilesItem>) {

        val preSelectedItems = contacts.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }

        this.originalProfileList = contacts
        this.filteredProfileList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values  = originalProfileList
            } else {
                val filteredList = ArrayList<JobProfilesItem>()
                for (contact in originalProfileList) {
                    if (contact.name?.contains(
                            charString,
                            true
                        ) == true
                    )
                        filteredList.add(contact)
                }
                filterResults.values  = filteredList
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredProfileList = results?.values as ArrayList<JobProfilesItem>
            notifyDataSetChanged()
        }
    }


    inner class BusinessViewHolder(
            itemView: View
    ) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener {

        private var jobProfileNameTv: TextView = itemView.findViewById(R.id.job_profile_name_tv)
        private var jobProfileSelectedImageIV: ImageView = itemView.findViewById(R.id.job_profile_selected_iv)
        private var jobProfileRootLayout: LinearLayout = itemView.findViewById(R.id.job_profile_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: JobProfilesItem, position: Int) {
            jobProfileNameTv.text = jobProfile.name

            if (selectedId == jobProfile.id) {
                jobProfileRootLayout.background = ContextCompat.getDrawable(
                                        context,
                                        R.drawable.option_selection_border
                )
            } else {
                jobProfileRootLayout.setBackgroundResource(R.drawable.rectangle_2)
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition
            val jobProfile = filteredProfileList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = jobProfile.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = jobProfile.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onJobProfileSelectedListener?.onJobProfileSelected(
                jobProfile
            )
        }

    }

    fun getIndexFromId(
        id: String
    ): Int {
        val city = filteredProfileList.find { it.id == id }

        return if (city == null)
            return -1
        else
            filteredProfileList.indexOf(city)
    }



    interface OnJobProfileSelectedListener{

        fun onJobProfileSelected(jobProfileSelected : JobProfilesItem)
    }

}