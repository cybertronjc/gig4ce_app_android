package com.gigforce.giger_gigs

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.giger_gigs.listItems.AttendanceCompanyNameRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceGigerAttendanceRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceShiftTimeRecyclerItemView
import com.gigforce.giger_gigs.listItems.AttendanceStatusRecyclerItemView

class GigViewTypeLoader: IViewTypeLoader {

    override fun getView(context: Context, viewType: Int): View {
        return when(viewType){
            GigViewTypes.GIGER_ATTENDANCE -> AttendanceGigerAttendanceRecyclerItemView(context,null)
            GigViewTypes.ATTENDANCE_BUSSINESS_NAME -> AttendanceCompanyNameRecyclerItemView(context,null)
            GigViewTypes.ATTENDANCE_SHIFT_TIME -> AttendanceShiftTimeRecyclerItemView(context,null)
            GigViewTypes.ATTENDANCE_STATUS -> AttendanceStatusRecyclerItemView(context,null)
            else -> {
                throw IllegalStateException("GigViewTypeLoader() : View type not defined for $viewType")
            }
        }
    }
}

object GigViewTypes {

    const val GIGER_ATTENDANCE= 12234
    const val ATTENDANCE_BUSSINESS_NAME= 12235
    const val ATTENDANCE_SHIFT_TIME= 12236

    const val ATTENDANCE_STATUS= 12237
}