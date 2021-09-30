package com.gigforce.lead_management.ui.select_tls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
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

    private var selectedId: String? = null
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
        if (selectedId == null)
            return null
        else {
            return filteredTLList[getIndexFromId(selectedId!!)]
        }
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

        val preSelectedItems = tls.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }

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

            if (selectedId == businessTL.id) {
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
            val tlClicked = filteredTLList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = tlClicked.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = tlClicked.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onClientTLListener?.onClientTLSelected(
                tlClicked
            )
        }

    }

    fun getIndexFromId(
        id: String
    ): Int {
        val tl = filteredTLList.find { it.id == id }

        return if (tl == null)
            return -1
        else
            filteredTLList.indexOf(tl)
    }


    interface OnClientTLListener {

        fun onClientTLSelected(businessTl: BusinessTeamLeadersItem)
    }

}