package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_gigs.listItems.AttendanceBusinessAndShiftTimeRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceGigerAttendanceRecyclerItemView

class GigViewTypeLoader: IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View {
        return when(viewType){
            GigViewTypes.GIGER_ATTENDANCE -> AttendanceGigerAttendanceRecyclerItemView(context,null)
            GigViewTypes.ATTENDANCE_BUSINESS_SHIFT_TIME -> AttendanceBusinessAndShiftTimeRecyclerItemView(context,null)
            else -> {
                throw IllegalStateException("GigViewTypeLoader() : View type not defined for $viewType")
            }
        }
    }
}

object GigViewTypes {

    const val GIGER_ATTENDANCE= 12234
    const val ATTENDANCE_BUSINESS_SHIFT_TIME= 12236
}