package com.gigforce.app.tl_work_space.upcoming_gigers

import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
import com.gigforce.core.recyclerView.CoreDiffUtilCallback

class UpcomingGigersAdapterDiffUtil : CoreDiffUtilCallback<UpcomingGigersListData>() {


    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is UpcomingGigersListData.BusinessItemData &&
                newItem is UpcomingGigersListData.BusinessItemData
            ) {
                return oldItem.businessName == newItem.businessName
            } else if (oldItem is UpcomingGigersListData.UpcomingGigerItemData &&
                newItem is UpcomingGigersListData.UpcomingGigerItemData
            ) {
                return oldItem.gigerId == newItem.gigerId
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
                oldItem is UpcomingGigersListData.BusinessItemData &&
                newItem is UpcomingGigersListData.BusinessItemData
            ) {

                return oldItem.businessName == newItem.businessName
            } else if (
                oldItem is UpcomingGigersListData.UpcomingGigerItemData &&
                newItem is UpcomingGigersListData.UpcomingGigerItemData
            ) {

                return oldItem.gigerId == newItem.gigerId &&
                        oldItem.gigerName == newItem.gigerName &&
                        oldItem.business == newItem.business &&
                        oldItem.jobProfile == newItem.jobProfile &&
                        oldItem.phoneNumber == newItem.phoneNumber &&
                        oldItem.profilePicture == newItem.profilePicture
            }

            return false
        }
    }

}