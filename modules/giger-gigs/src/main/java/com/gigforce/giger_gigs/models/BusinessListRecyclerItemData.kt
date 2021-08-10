package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.LoginSummaryViewTypes
import com.gigforce.giger_gigs.tl_login_details.AddNewLoginSummaryViewModel

open class BusinessListRecyclerItemData (
    val type: Int
) : SimpleDVM(type) {

    data class BusinessRecyclerItemData(
        val businessId: String,
        val businessName: String,
        val legalName: String,
        var loginCount: Int?,
        var updatedBy: String,
        val addNewLoginSummaryViewModel: AddNewLoginSummaryViewModel,
        var itemView: Int
    ) : BusinessListRecyclerItemData(
        LoginSummaryViewTypes.BusinessList
    )
}