package com.gigforce.lead_management.gigeronboarding.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.databinding.RecyclerRowJoiningStatusItemBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationStatusItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.models.JoiningListRecyclerItemData

class GigAppListStatusRecyclerItemView (
    context: Context,
    attrs: AttributeSet?
    ) : RelativeLayout(
    context,
    attrs
    ), IViewHolder {

    private lateinit var viewBinding: SelectGigApplicationStatusItemLayoutBinding

    init {
        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
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