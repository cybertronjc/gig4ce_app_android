package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.databinding.SelectGigApplicationStatusItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData

class GigAppListStatusRecyclerItemView (
    context: Context,
    attrs: AttributeSet?
    ) : RelativeLayout(
    context,
    attrs
    ), IViewHolder {

    private var viewBinding: SelectGigApplicationStatusItemLayoutBinding

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewBinding = SelectGigApplicationStatusItemLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }


    override fun bind(data: Any?) {
        data?.let {
            val statusData = it as GigAppListRecyclerItemData.GigAppListStatusRecyclerItemData
            viewBinding.statusTv.text = statusData.status
        }
    }
}