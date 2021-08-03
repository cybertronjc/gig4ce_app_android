package com.gigforce.giger_gigs.models

import com.gigforce.core.SimpleDVM
import com.gigforce.giger_gigs.LoginSummaryViewTypes

open class BusinessListRecyclerItemData (
    val type: Int
) : SimpleDVM(type) {

    data class BusinessRecyclerItemData(
        val id: String,
        val businessId: String,
        val businessName: String,
        val legalName: String,
        var loginCount: Int
    ) : BusinessListRecyclerItemData(
        LoginSummaryViewTypes.BusinessList
    )
}