package com.gigforce.app.tl_work_space.home

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceCardItemData
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.recyclerView.CoreDiffUtilCallback

class TLWorkSpaceHomeAdapterDiffUtil : CoreDiffUtilCallback<TLWorkspaceRecyclerItemData>() {


    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData
            ) {
                return oldItem.sectionId == newItem.sectionId
            } else if (oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData
            ) {
                return oldItem.sectionId == newItem.sectionId
            } else if (oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData
            ) {
                return true
            }

            return false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (
                oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceType1RecyclerItemData
            ) {

                return oldItem.sectionId == newItem.sectionId &&
                        oldItem.sectionTitle == newItem.sectionTitle &&
                        (oldItem.currentFilter?.filterId != null && newItem.currentFilter?.filterId != null) &&
                        oldItem.currentFilter.filterId == newItem.currentFilter.filterId &&
                        oldItem.itemData.hasSameContentAs(newItem.itemData)

            } else if (
                oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceType2RecyclerItemData
            ) {

                return oldItem.sectionId == newItem.sectionId &&
                        oldItem.sectionTitle == newItem.sectionTitle &&
                        (oldItem.currentFilter?.filterId != null && newItem.currentFilter?.filterId != null) &&
                        oldItem.currentFilter.filterId == newItem.currentFilter.filterId &&
                        oldItem.itemData.hasSameContentAs(newItem.itemData)
            } else if(
                oldItem is TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData &&
                newItem is TLWorkspaceRecyclerItemData.TLWorkspaceUpcomingGigersRecyclerItemData
            ){

               return  oldItem.sectionId == newItem.sectionId &&
                       oldItem.upcomingGigers.hasSameGigersAs(newItem.upcomingGigers)
            }

            return false
        }
    }


    private fun List<TLWorkspaceCardItemData>.hasSameContentAs(
        otherList: List<TLWorkspaceCardItemData>
    ): Boolean {
        if (this.size != otherList.size) {
            return false
        }

        for ((index, value) in this.withIndex()) {

            if (!value.hasSameContentAs(otherList[index])) {
                return false
            }
        }
        return true
    }

    private fun List<UpcomingGigersApiModel>.hasSameGigersAs(
        otherList: List<UpcomingGigersApiModel>
    ): Boolean {
        if (this.size != otherList.size) {
            return false
        }

        for ((index, value) in this.withIndex()) {

            if (!value.hasSameContentAs(otherList[index])) {
                return false
            }
        }
        return true
    }

}