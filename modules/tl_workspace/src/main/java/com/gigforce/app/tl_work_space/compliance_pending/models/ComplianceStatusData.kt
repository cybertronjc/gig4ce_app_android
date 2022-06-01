package com.gigforce.app.tl_work_space.compliance_pending.models

import com.gigforce.app.domain.models.tl_workspace.retention.StatusMasterWithCountItem
import com.gigforce.app.tl_work_space.compliance_pending.CompliancePendingViewModel

data class ComplianceStatusData(
    val id: String,
    val title: String,
    var value: Int,
    var selected : Boolean,
    val viewModel: CompliancePendingViewModel
) {

    companion object {
        fun fromAPiModel(
            statusMasterWithCountItem: StatusMasterWithCountItem,
            viewModel: CompliancePendingViewModel
        ): ComplianceStatusData {
            return ComplianceStatusData(
                title = statusMasterWithCountItem.title ?: "",
                value = statusMasterWithCountItem.count ?: 0,
                viewModel = viewModel,
                selected = false,
                id = statusMasterWithCountItem.id
                    ?: throw IllegalArgumentException("id found null in retention status")
            )
        }
    }
}