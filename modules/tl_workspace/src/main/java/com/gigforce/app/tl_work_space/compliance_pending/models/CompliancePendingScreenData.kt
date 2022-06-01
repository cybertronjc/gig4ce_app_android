package com.gigforce.app.tl_work_space.compliance_pending.models

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.compliance_pending.CompliancePendingViewModel
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel
import com.gigforce.core.SimpleDVM
import kotlin.random.Random

open class CompliancePendingScreenData(
    val type: Int
) : SimpleDVM(type) {

    data class GigerItemData(
        val gigerId: String,
        val gigerName: String,
        val phoneNumber: String?,
        val business: String? ,
        val jobProfile: String? ,
        val profilePicture: String? ,
        val profilePictureThumbnail: String?,
        val selectionDateString : String,
        val warningText : String?,
        val viewModel: CompliancePendingViewModel
    ) : CompliancePendingScreenData(TLWorkSpaceCoreRecyclerViewBindings.ComplianceGigerItemType){


    }

    data class BusinessItemData(
        val businessName: String,
        val count: Int,
        val expanded: Boolean,
        val viewModel: CompliancePendingViewModel
    ) : CompliancePendingScreenData(TLWorkSpaceCoreRecyclerViewBindings.ComplianceBusinessItemType)
}