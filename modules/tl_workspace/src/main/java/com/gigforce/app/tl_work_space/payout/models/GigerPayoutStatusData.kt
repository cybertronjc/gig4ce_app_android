package com.gigforce.app.tl_work_space.payout.models

import com.gigforce.app.domain.models.tl_workspace.payout.StatusMasterWithCountItem
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.app.tl_work_space.payout.GigerPayoutViewModel

open class GigerPayoutStatusData(
     val id: String,
     val title: String,
     var value: Int,
     val countChangedBy: Int,
     var selected : Boolean,
     val changeType: ValueChangeType,
     val viewModel: GigerPayoutViewModel
) {

    companion object {
        fun fromAPiModel(
            statusMasterWithCountItem: StatusMasterWithCountItem,
            viewModel: GigerPayoutViewModel
        ): GigerPayoutStatusData {
            return GigerPayoutStatusData(
                title = statusMasterWithCountItem.title ?: "",
                value = statusMasterWithCountItem.count ?: 0,
                countChangedBy = statusMasterWithCountItem.countChangedBy ?: 0,
                changeType = ValueChangeType.fromChangeString(statusMasterWithCountItem.countChangeType),
                viewModel = viewModel,
                selected = false,
                id = statusMasterWithCountItem.id
                    ?: throw IllegalArgumentException("RetentionTabData: fromAPiModel() -id found null in retention status")
            )
        }
    }
}