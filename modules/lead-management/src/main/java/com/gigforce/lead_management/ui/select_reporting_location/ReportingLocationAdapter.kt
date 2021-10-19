package com.gigforce.lead_management.ui.select_reporting_location

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.viewdatamodels.leadManagement.ReportingLocationsItem
import com.gigforce.lead_management.R

class ReportingLocationAdapter(
        private val context: Context,
        private val requestManager: RequestManager
) : RecyclerView.Adapter<ReportingLocationAdapter.BusinessViewHolder>(),
        Filterable {

    private var originalLocationList= listOf<ReportingLocationsItem>()
    private var filteredLocationList= listOf<ReportingLocationsItem>()

    private val contactsFilter = CityFilter()

    private var selectedId: String? = null
    private var onReportingLocationSelectedListener: OnReportingLocationSelectedListener? = null

    fun setOnReportingLocationSelectedListener(onReportingLocationSelectedListener: OnReportingLocationSelectedListener) {
        this.onReportingLocationSelectedListener = onReportingLocationSelectedListener
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

    fun getSelectedReportingLocation() : ReportingLocationsItem?{
        if (selectedId == null)
            return null
        else {
            return filteredLocationList[getIndexFromId(selectedId!!)]
        }
    }

    override fun getItemCount(): Int {
        return filteredLocationList.size
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        if(position != RecyclerView.NO_POSITION
                && position < filteredLocationList.size
        ) {
            holder.bindValues(filteredLocationList.get(position), position)
        }
    }

    fun setData(contacts: List<ReportingLocationsItem>) {

        val preSelectedItems = contacts.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }

        this.originalLocationList = contacts
        this.filteredLocationList = contacts
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values  = originalLocationList
            } else {
                val filteredList = ArrayList<ReportingLocationsItem>()
                for (contact in originalLocationList) {
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
            val filterResult = results?.values as? ArrayList<ReportingLocationsItem>
            filteredLocationList = if (filterResult.isNullOrEmpty()) emptyList() else filterResult
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

        fun bindValues(jobProfile: ReportingLocationsItem, position: Int) {
            jobProfileNameTv.text = jobProfile.name

            if (selectedId == jobProfile.id) {
                jobProfileRootLayout.background = ContextCompat.getDrawable(
                                        context,
                                        R.drawable.option_selection_border
                )

                jobProfileSelectedImageIV.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_selected_tick,
                        null
                    )
                )
            } else {
                jobProfileRootLayout.setBackgroundResource(R.drawable.rectangle_2)

                jobProfileSelectedImageIV.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_unselect_tick,
                        null
                    )
                )
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition
            val locationClicked = filteredLocationList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = locationClicked.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = locationClicked.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onReportingLocationSelectedListener?.onReportingLocationSelected(
                locationClicked
            )
        }

    }


    fun getIndexFromId(
        id: String
    ): Int {
        val tl = filteredLocationList.find { it.id == id }

        return if (tl == null)
            return -1
        else
            filteredLocationList.indexOf(tl)
    }



    interface OnReportingLocationSelectedListener{

        fun onReportingLocationSelected(reportingLocation : ReportingLocationsItem)
    }

}