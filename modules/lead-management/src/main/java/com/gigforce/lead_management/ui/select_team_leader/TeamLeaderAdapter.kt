package com.gigforce.lead_management.ui.select_team_leader

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.viewdatamodels.leadManagement.TeamLeader
import com.gigforce.lead_management.BR
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.RecyclerItemTeamLeaderBinding

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

        return TeamLeaderViewHolder(
            RecyclerItemTeamLeaderBinding.inflate(
                LayoutInflater.from(
                    parent.context
                ),
                parent,
                false
            )
        )
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
            holder.bindValues(filteredTLList.get(position))
        }
    }

    fun setData(teamLeaders: ArrayList<TeamLeader>) {

        val preSelectedItems = teamLeaders.filter {
            it.selected
        }

        if (preSelectedItems.isNotEmpty()) {
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
                tlCountVisibleAfterFiltering = filteredTLList.size,
                selectedTLVisible = filteredTLList.find { it.id == selectedId } != null
            )
        }
    }


    inner class TeamLeaderViewHolder(
        private val itemViewBinding: RecyclerItemTeamLeaderBinding
    ) : RecyclerView.ViewHolder(
        itemViewBinding.root
    ), View.OnClickListener {


        init {
            itemView.setOnClickListener(this)
        }

        fun bindValues(
            teamLeader: TeamLeader
        ) {
            itemViewBinding.teamLeader = teamLeader
            itemViewBinding.teamLeaderProfilePictureIv.loadProfilePicture(
                teamLeader.profilePictureThumbnail,
                teamLeader.profilePicture
            )

            if (selectedId == teamLeader.id) {

                itemViewBinding.jobProfileRootLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.option_selection_border
                )
                itemViewBinding.selectedIv.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_selected_tick,
                        null
                    )
                )
            } else {

                itemViewBinding.jobProfileRootLayout.setBackgroundResource(R.drawable.rectangle_2)
                itemViewBinding.selectedIv.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_unselect_tick,
                        null
                    )
                )
            }

            itemViewBinding.executePendingBindings()
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
            tlCountVisibleAfterFiltering: Int,
            selectedTLVisible: Boolean
        )

        fun onTeamLeaderSelected(
            selectedTL: TeamLeader
        )
    }

}