package com.gigforce.giger_gigs.listItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View.inflate
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.databinding.RecyclerRowBusinessNameShiftTimeBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData


class AttendanceBusinessAndShiftTimeRecyclerItemView(
        context: Context,
        attrs: AttributeSet?
) : RelativeLayout(
        context,
        attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerRowBusinessNameShiftTimeBinding

    init {
        setDefault()
        inflate()
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
        data?.let {
            val shiftNameData = it as AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData
            viewBinding.shiftTimeTv.text = shiftNameData.shiftName
            viewBinding.companyNameTv.text = shiftNameData.businessName
        }
    }
}
