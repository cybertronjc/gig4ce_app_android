package com.gigforce.app.tl_work_space.upcoming_gigers

import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData

object UpcomingGigersListProcessor {

    fun processRawUpcomingListForView(
        rawList: List<UpcomingGigersApiModel>,
        searchText: String?
    ): List<UpcomingGigersListData> {
        if (rawList.isEmpty()) return emptyList()

        return emptyList()
    }
}