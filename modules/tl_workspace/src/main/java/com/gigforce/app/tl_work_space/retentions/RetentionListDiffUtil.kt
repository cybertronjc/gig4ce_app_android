package com.gigforce.app.tl_work_space.retentions

import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.core.recyclerView.CoreDiffUtilCallback

class RetentionListDiffUtil : CoreDiffUtilCallback<RetentionScreenData>() {


    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is RetentionScreenData.BusinessItemData &&
                newItem is RetentionScreenData.BusinessItemData
            ) {
                return oldItem.businessName == newItem.businessName
            } else if (oldItem is RetentionScreenData.GigerItemData &&
                newItem is RetentionScreenData.GigerItemData
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
                oldItem is RetentionScreenData.BusinessItemData &&
                newItem is RetentionScreenData.BusinessItemData
            ) {

                return oldItem.businessName == newItem.businessName &&
                        oldItem.expanded == newItem.expanded &&
                        oldItem.count == newItem.count
            } else if (
                oldItem is RetentionScreenData.GigerItemData &&
                newItem is RetentionScreenData.GigerItemData
            ) {

                return oldItem.gigerId == newItem.gigerId &&
                        oldItem.gigerName == newItem.gigerName &&
                        oldItem.business == newItem.business &&
                        oldItem.jobProfile == newItem.jobProfile &&
                        oldItem.phoneNumber == newItem.phoneNumber &&
                        oldItem.profilePicture == newItem.profilePicture &&
                        oldItem.selectionDateString == newItem.selectionDateString &&
                        oldItem.warningText == newItem.warningText
            }

            return false
        }
    }

}