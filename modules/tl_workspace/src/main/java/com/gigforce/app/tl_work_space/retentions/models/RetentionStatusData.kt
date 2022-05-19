package com.gigforce.app.tl_work_space.retentions.models

import com.gigforce.app.domain.models.tl_workspace.retention.StatusMasterWithCountItem
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel

data class RetentionStatusData(
    val id: String,
    val title: String,
    val value: Int,
    val valueChangedBy: Int,
    val changeType: ValueChangeType,
    val viewModel: RetentionViewModel
) {

    companion object {
        fun fromAPiModel(
            statusMasterWithCountItem: StatusMasterWithCountItem,
            viewModel: RetentionViewModel
        ): RetentionStatusData {
            return RetentionStatusData(
                title = statusMasterWithCountItem.title ?: "",
                value = statusMasterWithCountItem.count ?: 0,
                valueChangedBy = statusMasterWithCountItem.valueChangedBy ?: 0,
                changeType = ValueChangeType.fromChangeString(statusMasterWithCountItem.valueChangeType),
                viewModel = viewModel,
                id = statusMasterWithCountItem.id
                    ?: throw IllegalArgumentException("id found null in retention status")
            )
        }
    }
}