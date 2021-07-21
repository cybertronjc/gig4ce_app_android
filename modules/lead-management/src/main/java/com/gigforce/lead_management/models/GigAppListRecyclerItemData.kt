package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.gigeronboarding.SelectGigApplicationToActivateViewModel
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class GigAppListRecyclerItemData(
    val type: Int
) : SimpleDVM(type, onClickNavPath = null) {

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
        var selected: Boolean = false
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.GigAppList
    )

    data class GigAppListSearchRecyclerItemData(
        val search: String,
        val selectGigAppViewModel: SelectGigApplicationToActivateViewModel
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.GigAppListSearch
    )

    data class NoGigAppsFoundItemData(
        val message: String
    ) : GigAppListRecyclerItemData(
        LeadActivationViewTypes.NoGigAppsFound
    )
}