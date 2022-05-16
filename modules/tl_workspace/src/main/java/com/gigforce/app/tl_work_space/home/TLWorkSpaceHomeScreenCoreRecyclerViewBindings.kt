package com.gigforce.app.tl_work_space.home

import android.content.Context
import android.view.View
import com.gigforce.app.tl_work_space.home.views.*
import com.gigforce.core.IViewTypeLoader

object TLWorkSpaceHomeScreenCoreRecyclerViewBindings : IViewTypeLoader {

    const val TLWorkspaceType1ItemType = 342211
    const val TLWorkspaceType2ItemType = 3422433
    const val UpcomingGigersItemType = 342256

    //Inner TLWorkSpace Home Screen Items
    const val TLWorkspaceType1InnerCardType = 346622
    const val TLWorkspaceType2InnerCardType = 342215
    const val UpcomingGigersInnerItemType = 3422123

    override fun getView(
        context: Context,
        viewType: Int
    ): View? = when(viewType) {
        TLWorkspaceType1ItemType -> TLWorkspaceType1SectionView(context,null)
        TLWorkspaceType1InnerCardType -> TLWorkspaceType1SectionInnerCardView(context,null)

        TLWorkspaceType2ItemType -> TLWorkspaceType2SectionView(context,null)
        TLWorkspaceType2InnerCardType -> TLWorkspaceType2InnerCardView(context,null)

        UpcomingGigersItemType -> TLWorkspaceUpcomingGigersSectionView(context,null)
        UpcomingGigersInnerItemType -> TLWorkspaceUpcomingGigersItemView(context,null)
        else -> null
    }
}