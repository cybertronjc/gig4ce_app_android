package com.gigforce.app.tl_work_space.home.models

import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.UpcomingGigersApiModel
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeScreenCoreRecyclerViewBindings
import com.gigforce.core.SimpleDVM

open class TLWorkspaceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class TLWorkspaceType1RecyclerItemData(
        val sectionId: String,
        val sectionTitle : String,
        val currentFilter : TLWorkSpaceFilterOption?,
        val itemData : List<TLWorkspaceCardItemData>
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceHomeScreenCoreRecyclerViewBindings.TLWorkspaceType1ItemType
    )

    data class TLWorkspaceType2RecyclerItemData(
        val sectionId: String,
        val sectionTitle : String,
        val currentFilter : TLWorkSpaceFilterOption?,
        val itemData : List<TLWorkspaceCardItemData>
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceHomeScreenCoreRecyclerViewBindings.TLWorkspaceType2ItemType
    )

    data class TLWorkspaceUpcomingGigersRecyclerItemData(
        val sectionId: String,
        val upcomingGigers : List<UpcomingGigersApiModel>
    ) : TLWorkspaceRecyclerItemData(
        type = TLWorkSpaceHomeScreenCoreRecyclerViewBindings.UpcomingGigersItemType
    )
}