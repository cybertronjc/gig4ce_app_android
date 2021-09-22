package com.gigforce.lead_management.ui.select_tls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.gigforce.common_ui.viewdatamodels.leadManagement.BusinessTeamLeadersItem
import com.gigforce.lead_management.R

class ClientTLAdapter(
    private val context: Context,
    private val requestManager: RequestManager
) : RecyclerView.Adapter<ClientTLAdapter.BusinessViewHolder>(),
    Filterable {

    private var originalTLList = ArrayList<BusinessTeamLeadersItem>()
    private var filteredTLList = ArrayList<BusinessTeamLeadersItem>()

    private val contactsFilter = CityFilter()

    private var selectedItemIndex: Int = -1
    private var onClientTLListener: OnClientTLListener? = null

    fun setOnClientTLListener(onClientTLListener: OnClientTLListener) {
        this.onClientTLListener = onClientTLListener
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

    fun getSelectedTL(): BusinessTeamLeadersItem? {
        if (selectedItemIndex == -1)
            return null
        else {
            return filteredTLList[selectedItemIndex]
        }
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
        return filteredTLList.size
    }

    override fun onBindViewHolder(holder: BusinessViewHolder, position: Int) {

        if (position != RecyclerView.NO_POSITION
            && position < filteredTLList.size
        ) {
            holder.bindValues(filteredTLList.get(position), position)
        }
    }

    fun setData(tls: ArrayList<BusinessTeamLeadersItem>) {

        this.selectedItemIndex = -1
        this.originalTLList = tls
        this.filteredTLList = tls
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class CityFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values = originalTLList
            } else {
                val filteredList = ArrayList<BusinessTeamLeadersItem>()
                for (contact in originalTLList) {
                    if (contact.name?.contains(
                            charString,
                            true
                        ) == true
                    )
                        filteredList.add(contact)
                }
                filterResults.values = filteredList
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            filteredTLList = results?.values as ArrayList<BusinessTeamLeadersItem>
            notifyDataSetChanged()
        }
    }


    inner class BusinessViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var jobProfileNameTv: TextView = itemView.findViewById(R.id.job_profile_name_tv)
        private var jobProfileSelectedImageIV: ImageView =
            itemView.findViewById(R.id.job_profile_selected_iv)
        private var jobProfileRootLayout: LinearLayout =
            itemView.findViewById(R.id.job_profile_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(
            businessTL: BusinessTeamLeadersItem,
            position: Int
        ) {
            jobProfileNameTv.text = businessTL.name

            if (selectedItemIndex == position) {
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

            if (selectedItemIndex != -1) {
                val tempIndex = selectedItemIndex
                selectedItemIndex = newPosition
                notifyItemChanged(tempIndex)
                notifyItemChanged(selectedItemIndex)
            } else {
                selectedItemIndex = newPosition
                notifyItemChanged(selectedItemIndex)
            }

            onClientTLListener?.onClientTLSelected(
                filteredTLList[selectedItemIndex]
            )
        }

    }

    fun uncheckedSelection() {
        if (selectedItemIndex != -1) {
            var position = selectedItemIndex
            selectedItemIndex = -1
            notifyItemChanged(position)
        }
    }


    interface OnClientTLListener {

        fun onClientTLSelected(businessTl: BusinessTeamLeadersItem)
    }

}