package com.gigforce.lead_management.ui.giger_info.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.databinding.LayoutApplicationChecklistItemBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding

class AppCheckListRecyclerComponent  (
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder {

    private var viewBinding: LayoutApplicationChecklistItemBinding
    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = LayoutApplicationChecklistItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {

    }
}