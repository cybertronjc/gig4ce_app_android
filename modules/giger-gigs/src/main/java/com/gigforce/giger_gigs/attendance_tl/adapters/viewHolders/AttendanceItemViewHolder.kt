package com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders

import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.adapters.TLAttendanceAdapter
import com.gigforce.giger_gigs.attendance_tl.adapters.TLAttendanceViewHolderAdapterInteraction
import com.gigforce.giger_gigs.databinding.RecyclerRowGigerAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class AttendanceItemViewHolder(
    private val viewBinding: RecyclerRowGigerAttendanceBinding,
    private val viewHolderAdapterInteraction: TLAttendanceViewHolderAdapterInteraction,
    private val itemClickListener: TLAttendanceAdapter.TLAttendanceAdapterClickListener
) : RecyclerView.ViewHolder(
    viewBinding.root
), View.OnClickListener {

    init {
        viewBinding.root.setOnClickListener(this)
        viewBinding.userImageIv.setOnClickListener(this)
        viewBinding.resolveBtn.setOnClickListener(this)
    }

    private lateinit var attendanceItem: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData

    fun getData(): AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData {
        return attendanceItem
    }

    fun getViewBinding(): RecyclerRowGigerAttendanceBinding {
        return viewBinding
    }

    fun bind(
        attendanceItem: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {
        this.attendanceItem = attendanceItem
        viewBinding.userImageIv.loadProfilePicture(
            attendanceItem.gigerImage,
            attendanceItem.gigerImage
        )
        viewBinding.attendanceModel = attendanceItem
        viewBinding.executePendingBindings()

        viewBinding.resolveLayout.isVisible = attendanceItem.hasAttendanceConflict()
    }

    override fun onClick(v: View?) {

        if (adapterPosition == View.NO_ID) return
        val clickedItem = viewHolderAdapterInteraction.getItemAt(adapterPosition) ?: return
        when (v?.id) {
            R.id.user_image_iv -> itemClickListener.onProfilePictureOfGigerClicked(
                clickedItem as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            )
            R.id.resolve_btn -> itemClickListener.onResolveClicked(
                clickedItem as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            )
            else -> itemClickListener.onAttendanceItemClicked(
                clickedItem as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            )
        }
    }
}