package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import com.gigforce.core.recyclerView.CoreDiffUtilCallback
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData

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
                        oldItem.activeCount == newItem.activeCount &&
                        oldItem.currentlySelectedStatus == newItem.currentlySelectedStatus

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