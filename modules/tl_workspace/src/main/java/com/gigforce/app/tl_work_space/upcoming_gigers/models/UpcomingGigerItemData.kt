package com.gigforce.app.tl_work_space.upcoming_gigers.models

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.upcoming_gigers.UpcomingGigersViewModel
import com.gigforce.core.SimpleDVM

open class UpcomingGigersListData(
    val type: Int
) : SimpleDVM(type) {

    data class UpcomingGigerItemData(
        val gigerId: String,
        val gigerName: String,
        val phoneNumber: String?,
        val business: String? = null,
        val jobProfile: String? = null,
        val profilePicture: String? = null,
        val profilePictureThumbnail: String? = null,
        val viewModel: UpcomingGigersViewModel
    ) : UpcomingGigersListData(TLWorkSpaceCoreRecyclerViewBindings.UpcomingGigersItemType)

    data class BusinessItemData(
        val businessName : String
    ): UpcomingGigersListData(TLWorkSpaceCoreRecyclerViewBindings.UpcomingGigersItemType)
}