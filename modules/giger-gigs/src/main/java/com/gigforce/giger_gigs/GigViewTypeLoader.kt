package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.giger_gigs.listItems.AttendanceBusinessAndShiftTimeRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceGigerAttendanceRecyclerItemView

class GigViewTypeLoader : IViewTypeLoader {

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
            else -> null
        }
    }
}

//object GigViewTypes {
//
//    const val GIGER_ATTENDANCE = 12234
//    const val ATTENDANCE_BUSINESS_SHIFT_TIME = 12236
//}