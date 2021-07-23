package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class GigAppListRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class GigAppListStatusRecyclerItemData(
        val status: String
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.GigAppListStatus
    )

    data class GigAppRecyclerItemData(
        val userUid: String,
        val status: String,
        val businessName: String,
        val jobProfileTitle: String,
        val businessLogo: String,
        val businessLogoThumbnail: String,
        val isSelected : Boolean
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.GigAppList
    )

    data class GigAppListSearchRecyclerItemData(
        val search: String
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.GigAppListSearch
    )
}