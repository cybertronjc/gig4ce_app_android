package com.gigforce.giger_gigs.attendance_tl.adapters

import androidx.recyclerview.widget.DiffUtil
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class TLAttendanceAdapterDiffUtil(
    private val oldList: List<AttendanceRecyclerItemData>,
    private val newList: List<AttendanceRecyclerItemData>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean {
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.type != newItem.type) {
            return false
        } else {
            if (oldItem is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData &&
                newItem is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
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
            if (oldItem is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData &&
                newItem is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
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
                        oldItem.attendanceStatus == newItem.attendanceStatus &&
                        oldItem.gigStatus == newItem.gigStatus
            }

            return false
        }
    }


}