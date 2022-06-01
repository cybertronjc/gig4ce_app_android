package com.gigforce.app.tl_work_space.payout

import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.core.recyclerView.CoreDiffUtilCallback

class GigerPayoutListDiffUtil : CoreDiffUtilCallback<GigerPayoutScreenData>(){
    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is GigerPayoutScreenData.BusinessItemData &&
                newItem is GigerPayoutScreenData.BusinessItemData
            ) {
                return oldItem.businessName == newItem.businessName
            } else if (oldItem is GigerPayoutScreenData.GigerItemData &&
                newItem is GigerPayoutScreenData.GigerItemData
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
                oldItem is GigerPayoutScreenData.BusinessItemData &&
                newItem is GigerPayoutScreenData.BusinessItemData
            ) {

                return oldItem.businessName == newItem.businessName &&
                        oldItem.expanded == newItem.expanded &&
                        oldItem.count == newItem.count
            } else if (
                oldItem is GigerPayoutScreenData.GigerItemData &&
                newItem is GigerPayoutScreenData.GigerItemData
            ) {

                return oldItem.gigerId == newItem.gigerId &&
                        oldItem.gigerName == newItem.gigerName &&
                        oldItem.business == newItem.business &&
                        oldItem.jobProfile == newItem.jobProfile &&
                        oldItem.phoneNumber == newItem.phoneNumber &&
                        oldItem.profilePicture == newItem.profilePicture &&
                        oldItem.selectionDateString == newItem.selectionDateString
//                        oldItem.warningText == newItem.warningText
            }

            return false
        }
    }


}