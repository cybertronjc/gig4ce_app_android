package com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders

import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class AttendanceBusinessCountViewHolder(
    private val viewBinding : RecyclerRowBusinessNameShiftTimeBinding
) : RecyclerView.ViewHolder(
    viewBinding.root
) {

    fun bind(
        businessAndAttendanceCount : AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
    ){

    }
}