package com.gigforce.lead_management.ui.select_cluster

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.viewdatamodels.leadManagement.OtherCityClusterItem
import com.gigforce.lead_management.R

class ClusterAdapter(
    private val context: Context,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<ClusterAdapter.BusinessViewHolder>(),
    Filterable {

    private var originalProfileList= ArrayList<OtherCityClusterItem>()
    private var filteredProfileList= ArrayList<OtherCityClusterItem>()

    private val contactsFilter = CityFilter()

    private var selectedId: String? = null
    //    private var selectedItemIndex: Int = -1

    private var onClusterSelectedListener: OnClusterSelectedListener? = null

    fun setOnClusterSelectedListener(onClusterSelectedListener: OnClusterSelectedListener) {
        this.onClusterSelectedListener = onClusterSelectedListener
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

    fun getSelectedBusiness() : OtherCityClusterItem?{
        if (selectedId == null)
            return null
        else {
            return originalProfileList[getIndexFromIdFromOriginalList(selectedId!!)]
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

    fun setData(contacts: ArrayList<OtherCityClusterItem>) {

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
                val filteredList = ArrayList<OtherCityClusterItem>()
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
            filteredProfileList = results?.values as ArrayList<OtherCityClusterItem>
            notifyDataSetChanged()

            onClusterSelectedListener?.onJobProfileFiltered(
                jobProfileCountVisibleAfterFiltering =  filteredProfileList.size,
                selectedJobProfileVisible =  filteredProfileList.find { it.id == selectedId } != null
            )
        }
    }


    inner class BusinessViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var jobProfileNameTv: TextView = itemView.findViewById(R.id.job_profile_name_tv)
        private var clusterSelectedImageIV: ImageView = itemView.findViewById(R.id.job_profile_selected_iv)
        private var jobProfileRootLayout: LinearLayout = itemView.findViewById(R.id.job_profile_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(jobProfile: OtherCityClusterItem, position: Int) {
            jobProfileNameTv.text = jobProfile.name

            if (selectedId == jobProfile.id) {
                jobProfileRootLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.option_selection_border
                )
                clusterSelectedImageIV.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_selected_tick,
                        null
                    )
                )
            } else {
                jobProfileRootLayout.setBackgroundResource(R.drawable.rectangle_2)

                clusterSelectedImageIV.setImageDrawable(
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

            onClusterSelectedListener?.onClusterSelected(
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

    fun getIndexFromIdFromOriginalList(
        id: String
    ): Int {
        val city = originalProfileList.find { it.id == id }

        return if (city == null)
            return -1
        else
            originalProfileList.indexOf(city)
    }



    interface OnClusterSelectedListener{

        fun onJobProfileFiltered(
            jobProfileCountVisibleAfterFiltering : Int,
            selectedJobProfileVisible : Boolean
        )


        fun onClusterSelected(clusterSelected : OtherCityClusterItem)
    }

}