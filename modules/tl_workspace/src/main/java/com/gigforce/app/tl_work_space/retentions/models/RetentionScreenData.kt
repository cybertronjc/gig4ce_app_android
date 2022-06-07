package com.gigforce.app.tl_work_space.retentions.models

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel
import com.gigforce.core.SimpleDVM

open class RetentionScreenData(
    val type: Int
) : SimpleDVM(type) {

    data class GigerItemData(
        val gigerId: String,
        val gigerName: String,
        val phoneNumber: String?,
        val businessId: String? ,
        val business: String? ,
        val jobProfileId: String? ,
        val jobProfile: String? ,
        val profilePicture: String? ,
        val profilePictureThumbnail: String?,
        val selectionDateString : String,
        val warningText : String?,
        val viewModel: RetentionViewModel
    ) : RetentionScreenData(TLWorkSpaceCoreRecyclerViewBindings.RetentionGigerItemType)

    data class BusinessItemData(
        val businessName: String,
        val count: Int,
        val expanded: Boolean,
        val viewModel: RetentionViewModel
    ) : RetentionScreenData(TLWorkSpaceCoreRecyclerViewBindings.RetentionBusinessItemType)
}