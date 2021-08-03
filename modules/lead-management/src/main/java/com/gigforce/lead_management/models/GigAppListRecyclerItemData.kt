package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.ui.select_gig_application.SelectGigApplicationToActivateViewModel
import com.gigforce.lead_management.ui.share_application_link.ShareApplicationLinkViewModel
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
        val status: String,
        val jobProfileId: String,
        val tradeName: String,
        val profileName: String,
        val companyLogo: String,
        val ongoing: Boolean,
        var selected: Boolean = false,
        val selectGigAppViewModel: SelectGigApplicationToActivateViewModel? = null,
        val shareApplicationLinkViewModel: ShareApplicationLinkViewModel? = null
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