package com.gigforce.giger_gigs.attendance_tl.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders.AttendanceBusinessCountViewHolder
import com.gigforce.giger_gigs.attendance_tl.adapters.viewHolders.AttendanceItemViewHolder
import com.gigforce.giger_gigs.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.giger_gigs.databinding.RecyclerRowGigerAttendanceBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

interface TLAttendanceViewHolderAdapterInteraction {

    fun getItemAt(
        position: Int
    ): AttendanceRecyclerItemData?
}

class TLAttendanceAdapter constructor(
    private val itemClickListener: TLAttendanceAdapterClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>(),
    TLAttendanceViewHolderAdapterInteraction {

    companion object {
        const val VIEW_TYPE_BUSINESS_ATTENDANCE_COUNT = 1222
        const val VIEW_TYPE_ATTENDANCE_ITEM = 1244
    }

    private var attendanceList: List<AttendanceRecyclerItemData>? = null

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(
        attendanceList: List<AttendanceRecyclerItemData>
    ) {
        if (this.attendanceList == null) {
            this.attendanceList = attendanceList
            notifyDataSetChanged()
        } else {
            val diffResult = DiffUtil.calculateDiff(
                TLAttendanceAdapterDiffUtil(
                    this.attendanceList!!,
                    attendanceList
                )
            )
            this.attendanceList = attendanceList
            diffResult.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_BUSINESS_ATTENDANCE_COUNT -> AttendanceBusinessCountViewHolder(
                RecyclerRowBusinessNameShiftTimeBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                ),
                this,
                itemClickListener
            )
            VIEW_TYPE_ATTENDANCE_ITEM -> AttendanceItemViewHolder(
                RecyclerRowGigerAttendanceBinding.inflate(
                    LayoutInflater.from(
                        parent.context
                    ),
                    parent,
                    false
                ),
                this,
                itemClickListener
            )
            else -> throw IllegalStateException("this view type is not supported")
        }
    }

    override fun getItemViewType(
        position: Int
    ): Int {
        return when (attendanceList!![position]) {
            is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData -> VIEW_TYPE_BUSINESS_ATTENDANCE_COUNT
            else -> VIEW_TYPE_ATTENDANCE_ITEM
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int
    ) {
        when (val item = attendanceList!![position]) {
            is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData -> {
                (holder as AttendanceBusinessCountViewHolder).bind(item)
            }
            is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData -> {
                (holder as AttendanceItemViewHolder).bind(item)
            }
        }
    }

    override fun getItemCount(): Int {
        return attendanceList?.size ?: 0
    }


    interface TLAttendanceAdapterClickListener {

        fun onCollapseOrExpandClickedForSomeBusiness(
            item: AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
        )

        fun onProfilePictureOfGigerClicked(
            item: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        )

        fun onAttendanceItemClicked(
            item: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        )

        fun onResolveClicked(
            item : AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
        )
    }

    override fun getItemAt(
        position: Int
    ): AttendanceRecyclerItemData? {
        return attendanceList?.get(position)
    }
}