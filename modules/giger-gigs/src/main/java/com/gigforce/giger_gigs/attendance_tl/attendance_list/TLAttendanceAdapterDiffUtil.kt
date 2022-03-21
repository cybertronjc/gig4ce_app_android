package com.gigforce.giger_gigs.attendance_tl.attendance_list

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.core.recyclerView.CoreDiffUtilCallback
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class TLAttendanceAdapterDiffUtil : CoreDiffUtilCallback<AttendanceRecyclerItemData>() {


    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData &&
                newItem is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData
            ) {
                return oldItem.businessName == newItem.businessName
            } else if (oldItem is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                newItem is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            ) {
                return oldItem.gigId == newItem.gigId
            }

            return false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData &&
                newItem is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData
            ) {

                return oldItem.businessName == newItem.businessName &&
                        oldItem.expanded == newItem.expanded &&
                        oldItem.inActiveCount == newItem.inActiveCount &&
                        oldItem.enabledCount == newItem.enabledCount &&
                        oldItem.activeCount == newItem.activeCount

            } else if (oldItem is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                newItem is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            ) {
                return oldItem.gigId == newItem.gigId &&
                        oldItem.status == newItem.status &&
                        oldItem.hasAttendanceConflict == newItem.hasAttendanceConflict &&
                        oldItem.currentlyMarkingAttendanceForThisGig == newItem.currentlyMarkingAttendanceForThisGig &&
                        oldItem.showGigerAttendanceLayout == newItem.showGigerAttendanceLayout &&
                        oldItem.gigerAttendanceStatus == newItem.gigerAttendanceStatus
            }

            return false
        }
    }


}