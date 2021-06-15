package com.gigforce.giger_gigs.listItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.databinding.RecyclerRowShiftTimeBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData


class AttendanceShiftTimeRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerRowShiftTimeBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowShiftTimeBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
            val shiftNameData = it as AttendanceRecyclerItemData.AttendanceRecyclerItemShiftNameData
            viewBinding.shiftTimeTv.text = shiftNameData.shiftName
        }
    }
}
