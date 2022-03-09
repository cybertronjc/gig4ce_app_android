package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.giger_gigs.attendance_tl.views.BusinessHeaderRecyclerItemView
import com.gigforce.giger_gigs.attendance_tl.views.GigerAttendanceItemRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceBusinessAndShiftTimeRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceGigerAttendanceRecyclerItemView

class GigCoreRecyclerViewBindings : IViewTypeLoader {

    companion object{

        const val VIEW_TYPE_TL_GIGER_ATTENDANCE_BUSINESS_HEADER  = 34351111
        const val VIEW_TYPE_TL_GIGER_ATTENDANCE_ITEM  = 34351166;
    }

    override fun getView(context: Context, viewType: Int): View? {
        return when (viewType) {
            CommonViewTypes.VIEW_GIGER_ATTENDANCE -> AttendanceGigerAttendanceRecyclerItemView(
                context,
                null
            )
            CommonViewTypes.VIEW_ATTENDANCE_BUSINESS_SHIFT_TIME -> AttendanceBusinessAndShiftTimeRecyclerItemView(
                context,
                null
            )
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