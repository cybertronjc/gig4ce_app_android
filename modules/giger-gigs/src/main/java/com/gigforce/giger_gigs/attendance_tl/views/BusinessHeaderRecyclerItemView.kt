package com.gigforce.giger_gigs.attendance_tl.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData

class BusinessHeaderRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private lateinit var viewBinding: RecyclerRowBusinessNameShiftTimeBinding
    private var viewData: AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData? = null

    init {
        setDefault()
        inflate()
        setOnClickListener(this)
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowBusinessNameShiftTimeBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        viewData = null

        (data as AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData?)?.let {
            viewData = it

            viewBinding.businessAndAttendanceModel = it
            viewBinding.executePendingBindings()

//            viewBinding.statusTv.text = it.date
//
//            if (it.expanded) {
//
//                viewBinding.statusTv.setTextColor(
//                    ResourcesCompat.getColor(resources, R.color.pink_text, null)
//                )
//                viewBinding.dropdownView.setImageDrawable(
//                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
//                )
//            } else {
//
//                viewBinding.statusTv.setTextColor(
//                    ResourcesCompat.getColor(resources, R.color.text_grey, null)
//                )
//                viewBinding.dropdownView.setImageDrawable(
//                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
//                )
//            }
        }
    }

    override fun onClick(
        p0: View?
    ) {
        viewData?.let {
           // it.viewModel.handleEvent(PayoutListViewContract.UiEvent.MonthYearHeaderClicked(it))
        }
    }
}
