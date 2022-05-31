package com.gigforce.app.tl_work_space

import android.content.Context
import android.view.View
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.views.BusinessHeaderRecyclerItemView
import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.views.GigerAttendanceItemRecyclerItemView
import com.gigforce.app.tl_work_space.custom_tab.TabWithCountAndChangeInfoCardView
import com.gigforce.app.tl_work_space.home.views.*
import com.gigforce.app.tl_work_space.retentions.views.RetentionBusinessItemView
import com.gigforce.app.tl_work_space.retentions.views.RetentionGigerItemView
import com.gigforce.app.tl_work_space.upcoming_gigers.views.UpcomingGigersBusinessItemView
import com.gigforce.app.tl_work_space.upcoming_gigers.views.UpcomingGigersItemView
import com.gigforce.core.IViewTypeLoader

object TLWorkSpaceCoreRecyclerViewBindings : IViewTypeLoader {


    /** Tab in Retention, Payout etc */
    const val CustomTabType1 = 3323312
    const val CustomTabType2 = 3323312
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
     * Upcoming Giger view [Attendance Tracker]
     */
    const val VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER  = 34351111
    const val VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM  = 34351166;

    /**
     * -----------------
     * Retention Fragments view [RetentionFragment]
     */
    const val RetentionGigerItemType = 392445
    const val RetentionBusinessItemType = 3374882

    override fun getView(
        context: Context,
        viewType: Int
    ): View? = when(viewType) {
        CustomTabType1 -> TabWithCountAndChangeInfoCardView(context,null)

        TLWorkspaceType1SectionItemType -> TLWorkspaceType1SectionView(context,null)
        TLWorkspaceType1InnerCardType -> TLWorkspaceType1SectionInnerCardView(context,null)

        TLWorkspaceType2SectionItemType -> TLWorkspaceType2SectionView(context,null)
        TLWorkspaceType2InnerCardType -> TLWorkspaceType2InnerCardView(context,null)

        UpcomingGigersSectionItemType -> TLWorkspaceUpcomingGigersSectionView(context,null)
        UpcomingGigersInnerItemType -> TLWorkspaceUpcomingGigersItemView(context,null)

        UpcomingGigersItemType -> UpcomingGigersItemView(context,null)
        UpcomingGigersBusinessItemType -> UpcomingGigersBusinessItemView(context,null)

        VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER -> BusinessHeaderRecyclerItemView(
            context,
            null
        )
        VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM -> GigerAttendanceItemRecyclerItemView(
            context,
            null
        )

        RetentionGigerItemType -> RetentionGigerItemView(context,null)
        RetentionBusinessItemType -> RetentionBusinessItemView(context,null)


        else -> null
    }
}