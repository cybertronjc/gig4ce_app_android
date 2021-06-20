package com.gigforce.giger_gigs.listItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.RecyclerRowAttendanceStatusBinding
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndViewModelData
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModel


class AttendanceStatusRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowAttendanceStatusBinding
    private lateinit var viewModel : GigerAttendanceUnderManagerViewModel
    private lateinit var statusData : AttendanceStatusAndCountItemData

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        setOnClickListener(this)
    }

    private fun inflate() {
        viewBinding = RecyclerRowAttendanceStatusBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
            val statusDataAndViewModel = it as AttendanceStatusAndViewModelData
            statusData = statusDataAndViewModel.statuses
            viewModel = statusDataAndViewModel.viewModel
            viewBinding.statusTv.text = statusData.status
            viewBinding.statusCountTv.text = statusData.attendanceCount.toString()

            if (statusData.statusSelected) {

                viewBinding.statusLayout.setBackgroundResource(
                    R.drawable.status_selected_border
                )
                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.lipstick_2,
                        null
                    )
                )
                viewBinding.statusCountTv.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.lipstick_2,
                        null
                    )
                )
            } else {

                viewBinding.statusLayout.setBackgroundResource(R.drawable.status_unselected_border)
                viewBinding.statusTv.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.black_three,
                        null
                    )
                )
                viewBinding.statusCountTv.setTextColor(
                    ResourcesCompat.getColor(
                        context.resources,
                        R.color.black_three,
                        null
                    )
                )
            }

        }
    }

    override fun onClick(v: View?) {
        viewModel.filterAttendanceByStatus(
            if(statusData.status == "All") null else statusData.status
        )
    }
}
