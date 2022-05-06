package com.gigforce.app.tl_work_space.home.models

import com.gigforce.core.SimpleDVM

open class TLWorkspaceRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class TLWorkspaceType1RecyclerItemData(
        val sectionId : String
    ) : TLWorkspaceRecyclerItemData(
        type =
    )

    data class TLWorkspaceType2RecyclerItemData(
        val sectionId : String,
        ): TLWorkspaceRecyclerItemData(
        type =
    )

    data class TLWorkspaceUpcomingGigersRecyclerItemData(
        val sectionId : String,

        ): TLWorkspaceRecyclerItemData(
        type =
    )
}