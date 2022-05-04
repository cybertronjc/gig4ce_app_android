package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_gigs.attendance_tl.attendance_list.views.BusinessHeaderRecyclerItemView
import com.gigforce.giger_gigs.attendance_tl.attendance_list.views.GigerAttendanceItemRecyclerItemView

class GigCoreRecyclerViewBindings : IViewTypeLoader {

    companion object{

        const val VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER  = 34351111
        const val VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM  = 34351166;
    }

    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER -> BusinessHeaderRecyclerItemView(
                context,
                null
            )
            VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM -> GigerAttendanceItemRecyclerItemView(
                context,
                null
            )
            else -> null
        }
    }
}

//object GigViewTypes {
//
//    const val GIGER_ATTENDANCE = 12234
//    const val ATTENDANCE_BUSINESS_SHIFT_TIME = 12236
//}