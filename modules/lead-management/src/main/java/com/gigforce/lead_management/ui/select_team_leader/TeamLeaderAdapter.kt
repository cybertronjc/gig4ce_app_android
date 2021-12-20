package com.gigforce.lead_management.ui.select_team_leader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.core.TextDrawable
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.lead_management.R

class TeamLeaderAdapter(
    private val context: Context
) : RecyclerView.Adapter<TeamLeaderAdapter.TeamLeaderViewHolder>(),
    Filterable {

    private var originalTLList = ArrayList<TeamLeader>()
    private var filteredTLList = ArrayList<TeamLeader>()

    private val contactsFilter = TeamLeaderFilter()

    private var selectedId: String? = null
    private var onTLSelectedListener: OnTeamLeaderSelectedListener? = null

    fun setOnTLSelectedListener(
        onTLSelectedListener: OnTeamLeaderSelectedListener
    ) {
        this.onTLSelectedListener = onTLSelectedListener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TeamLeaderViewHolder {
        val view = LayoutInflater.from(
            parent.context
        ).inflate(
            R.layout.recycler_item_city,
            parent,
            false
        )
        return TeamLeaderViewHolder(view)
    }

    fun getSelectedTL(): TeamLeader? {
        return if (selectedId == null)
            null
        else {
            originalTLList[getIndexFromIdOriginalList(selectedId!!)]
        }
    }

    override fun getItemCount(): Int {
        return filteredTLList.size
    }

    override fun onBindViewHolder(holder: TeamLeaderViewHolder, position: Int) {

        if (position != RecyclerView.NO_POSITION
            && position < filteredTLList.size
        ) {
            holder.bindValues(filteredTLList.get(position), position)
        }
    }

    fun setData(teamLeaders: ArrayList<TeamLeader>) {

        val preSelectedItems = teamLeaders.filter {
            it.selected
        }

        if(preSelectedItems.isNotEmpty()){
            this.selectedId = preSelectedItems.first().id
        } else {
            this.selectedId = null
        }
        this.originalTLList = teamLeaders
        this.filteredTLList = teamLeaders

        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = contactsFilter

    private inner class TeamLeaderFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val charString = constraint.toString()

            val filterResults = FilterResults()
            if (charString.isEmpty()) {
                filterResults.values = originalTLList
            } else {
                val filteredList = ArrayList<TeamLeader>()
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
            filteredTLList = results?.values as ArrayList<TeamLeader>
            notifyDataSetChanged()

            onTLSelectedListener?.onTeamLeaderFiltered(
                tlCountVisibleAfterFiltering =  filteredTLList.size,
                selectedTLVisible   =  filteredTLList.find { it.id == selectedId } != null
            )
        }
    }


    inner class TeamLeaderViewHolder(
        itemView: View
    ) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {

        private var businessNameTv: TextView = itemView.findViewById(R.id.city_name_tv)
        private var businessImageIV: ImageView = itemView.findViewById(R.id.city_image_iv)
        private var businessRootLayout: LinearLayout = itemView.findViewById(R.id.city_root_layout)

        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(business: TeamLeader, position: Int) {

            val companyInitials = if (business.name.isNullOrBlank())
                "C"
            else
                business.name!![0].toString().toUpperCase()

            val drawable = TextDrawable.builder().buildRound(
                companyInitials,
                ResourcesCompat.getColor(context.resources, R.color.lipstick, null)
            )
            businessImageIV.setImageDrawable(drawable)
//            }
//            else{
//                requestManager.load(business.icon).into(businessImageIV)
//            }

            businessNameTv.text = business.name

            if (selectedId == business.id) {
                businessRootLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.option_selection_border
                )
            } else {
                businessRootLayout.setBackgroundResource(R.drawable.rectangle_2)
            }
        }

        override fun onClick(v: View?) {

            val newPosition = adapterPosition
            val tl = filteredTLList[newPosition]

            if (selectedId != null) {
                val tempIndex = getIndexFromId(selectedId!!)
                selectedId = tl.id
                notifyItemChanged(tempIndex)
                notifyItemChanged(getIndexFromId(selectedId!!))
            } else {
                selectedId = tl.id
                notifyItemChanged(getIndexFromId(selectedId!!))
            }

            onTLSelectedListener?.onTeamLeaderSelected(
                tl
            )
        }
    }

    fun getIndexFromId(
        id: String
    ): Int {
        val city = filteredTLList.find { it.id == id }

        return if (city == null)
            return -1
        else
            filteredTLList.indexOf(city)
    }

    private fun getIndexFromIdOriginalList(
        id: String
    ): Int {
        val city = originalTLList.find { it.id == id }

        return if (city == null)
            return -1
        else
            originalTLList.indexOf(city)
    }


    interface OnTeamLeaderSelectedListener {

        fun onTeamLeaderFiltered(
            tlCountVisibleAfterFiltering : Int,
            selectedTLVisible : Boolean
        )

        fun onTeamLeaderSelected(
            selectedTL: TeamLeader
        )
    }

}