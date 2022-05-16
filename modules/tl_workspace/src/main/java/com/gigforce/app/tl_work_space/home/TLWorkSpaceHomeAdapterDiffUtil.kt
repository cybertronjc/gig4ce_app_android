package com.gigforce.app.tl_work_space.home

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
                        oldItem.itemData.hasSameType2ContentAs(newItem.itemData)
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


    private fun List<TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData>.hasSameContentAs(
        otherList: List<TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData>
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

    private fun List<TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData>.hasSameType2ContentAs(
        otherList: List<TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData>
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

    private fun List<TLWorkspaceRecyclerItemData.UpcomingGigerItemData>.hasSameGigersAs(
        otherList: List<TLWorkspaceRecyclerItemData.UpcomingGigerItemData>
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