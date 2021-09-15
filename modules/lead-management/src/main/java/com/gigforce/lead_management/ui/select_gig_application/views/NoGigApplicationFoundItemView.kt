package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.databinding.NoGigApplicationFoundBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class NoGigApplicationFoundItemView(
    context: Context,
    attrs: AttributeSet?
) : RelativeLayout(
    context,
    attrs
), IViewHolder {
    private var viewBinding: NoGigApplicationFoundBinding

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        viewBinding = NoGigApplicationFoundBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        data?.let {
            val statusData = it as GigAppListRecyclerItemData.NoGigAppsFoundItemData
            //viewBinding.title.text = statusData.message
        }


    }

}