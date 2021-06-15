package com.gigforce.giger_gigs.listItems

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.giger_gigs.databinding.RecyclerRowAttendanceCompanyNameBinding
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData


 class AttendanceCompanyNameRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: RecyclerRowAttendanceCompanyNameBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = RecyclerRowAttendanceCompanyNameBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
            val companyNameData = it as AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessData
            viewBinding.companyNameTv.text = companyNameData.businessName
        }
    }
}
