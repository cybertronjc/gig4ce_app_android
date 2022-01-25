package com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.attendance_tl.adapters.TLAttendanceAdapter
import com.gigforce.giger_gigs.attendance_tl.adapters.TLAttendanceViewHolderAdapterInteraction
import com.gigforce.giger_gigs.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class AttendanceBusinessCountViewHolder(
    private val viewBinding: RecyclerRowBusinessNameShiftTimeBinding,
    private val viewHolderAdapterInteration : TLAttendanceViewHolderAdapterInteraction,
    private val itemClickListener : TLAttendanceAdapter.TLAttendanceAdapterClickListener
) : RecyclerView.ViewHolder(
    viewBinding.root
), View.OnClickListener {

    init {
        viewBinding.root.setOnClickListener(this)
    }

    fun bind(
        businessAndAttendanceCount: AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
    ) {
        viewBinding.businessAndAttendanceModel = businessAndAttendanceCount
        viewBinding.executePendingBindings()
    }

    override fun onClick(
        v: View?
    ) {
        if(adapterPosition == View.NO_ID) return
        val clickedItem = viewHolderAdapterInteration.getItemAt(adapterPosition) ?: return
        itemClickListener.onCollapseOrExpandClickedForSomeBusiness(clickedItem as AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData)
    }
}