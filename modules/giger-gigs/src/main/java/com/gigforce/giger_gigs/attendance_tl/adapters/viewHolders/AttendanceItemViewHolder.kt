package com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders

import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.databinding.RecyclerRowGigerAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class AttendanceItemViewHolder(
    private val viewBinding: RecyclerRowGigerAttendanceBinding
) : RecyclerView.ViewHolder(
    viewBinding.root
) {

    fun bind(
        attendanceItem: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {

    }
}