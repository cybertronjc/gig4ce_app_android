package com.gigforce.app.tl_work_space.retentions

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData

object RetentionDataProcessor {

    fun processRawRetentionDataForListForView(
        rawUpcomingGigerList: List<UpcomingGigersApiModel>,
        searchText: String?,
        dateFilterOptionFromId: TLWorkSpaceFilterOption,
        retentionViewModel: RetentionViewModel
    ): List<UpcomingGigersListData> {
        TODO("Not yet implemented")
    }
}