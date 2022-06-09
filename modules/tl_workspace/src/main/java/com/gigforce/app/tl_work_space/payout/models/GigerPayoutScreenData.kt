package com.gigforce.app.tl_work_space.payout.models

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.payout.GigerPayoutViewModel
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel
import com.gigforce.core.SimpleDVM

open class GigerPayoutScreenData(
    val type: Int
) : SimpleDVM(type){

    data class GigerItemData(
        val payoutId : String,
        val gigerId: String,
        val gigerName: String,
        val phoneNumber: String?,
        val businessId: String? ,
        val business: String? ,
        val jobProfileId : String?,
        val jobProfile: String? ,
        val profilePicture: String? ,
        val profilePictureThumbnail: String?,
        val selectionDateString : String,
        val category: String?,
        val amount: Double?,
        val statusString: String,
        val status: String,
        val statusColorCode: String,
        val paymentDate: String?,
        val viewModel: GigerPayoutViewModel
    ) : GigerPayoutScreenData(TLWorkSpaceCoreRecyclerViewBindings.GigerPayoutGigerItemType)

    data class BusinessItemData(
        val businessName: String,
        val count: Int,
        val expanded: Boolean,
        val viewModel: GigerPayoutViewModel
    ) : GigerPayoutScreenData(TLWorkSpaceCoreRecyclerViewBindings.GigerPayoutBusinessItemType)
}