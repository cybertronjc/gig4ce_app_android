package com.gigforce.app.tl_work_space

import android.content.Context
import android.view.View
import com.gigforce.app.tl_work_space.home.views.*
import com.gigforce.app.tl_work_space.payout.GigerPayoutBusinessItemView
import com.gigforce.app.tl_work_space.payout.GigerPayoutGigerItemView
import com.gigforce.app.tl_work_space.retentions.views.RetentionBusinessItemView
import com.gigforce.app.tl_work_space.retentions.views.RetentionGigerItemView
import com.gigforce.app.tl_work_space.upcoming_gigers.views.UpcomingGigersBusinessItemView
import com.gigforce.app.tl_work_space.upcoming_gigers.views.UpcomingGigersItemView
import com.gigforce.core.IViewTypeLoader

object TLWorkSpaceCoreRecyclerViewBindings : IViewTypeLoader {

    /**
     * -----------------------------
     * TL Workspace HomeScreen Items
     * -----------------------------
     */
    const val TLWorkspaceType1SectionItemType = 342211
    const val TLWorkspaceType2SectionItemType = 3422433
    const val UpcomingGigersSectionItemType = 342256

    //Inner TLWorkSpace Home Screen Items
    const val TLWorkspaceType1InnerCardType = 346622
    const val TLWorkspaceType2InnerCardType = 342215
    const val UpcomingGigersInnerItemType = 3422123

    /**
     * -----------------
     * Upcoming Giger view [UpcomingGigersFragment]
     */
    const val UpcomingGigersItemType = 3421456
    const val UpcomingGigersBusinessItemType = 3427732

    /**
     * -----------------
     * Retention Fragments view [RetentionFragment]
     */
    const val RetentionGigerItemType = 392445
    const val RetentionBusinessItemType = 3374882

    /**
     * -----------------
     * GigerPayout Fragments view [RetentionFragment]
     */
    const val GigerPayoutGigerItemType = 392457
    const val GigerPayoutBusinessItemType = 3374890

    override fun getView(
        context: Context,
        viewType: Int
    ): View? = when(viewType) {
        TLWorkspaceType1SectionItemType -> TLWorkspaceType1SectionView(context,null)
        TLWorkspaceType1InnerCardType -> TLWorkspaceType1SectionInnerCardView(context,null)

        TLWorkspaceType2SectionItemType -> TLWorkspaceType2SectionView(context,null)
        TLWorkspaceType2InnerCardType -> TLWorkspaceType2InnerCardView(context,null)

        UpcomingGigersSectionItemType -> TLWorkspaceUpcomingGigersSectionView(context,null)
        UpcomingGigersInnerItemType -> TLWorkspaceUpcomingGigersItemView(context,null)

        UpcomingGigersItemType -> UpcomingGigersItemView(context,null)
        UpcomingGigersBusinessItemType -> UpcomingGigersBusinessItemView(context,null)

        RetentionGigerItemType -> RetentionGigerItemView(context,null)
        RetentionBusinessItemType -> RetentionBusinessItemView(context,null)

        GigerPayoutGigerItemType -> GigerPayoutGigerItemView(context, null)
        GigerPayoutBusinessItemType -> GigerPayoutBusinessItemView(context, null)


        else -> null
    }
}