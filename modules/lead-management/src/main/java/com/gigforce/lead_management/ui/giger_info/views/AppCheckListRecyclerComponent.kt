package com.gigforce.lead_management.ui.giger_info.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.core.IViewHolder
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.LayoutApplicationChecklistItemBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData
import com.gigforce.lead_management.models.GigAppListRecyclerItemData

class AppCheckListRecyclerComponent  (
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder {

    private var viewBinding: LayoutApplicationChecklistItemBinding
    private lateinit var viewData: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData
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
        data?.let {
            val applicationInfo =
                it as ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData
            viewData = applicationInfo

            viewBinding.checkListItemText.text = if (viewData.isOptional) viewData.checkName else viewData.checkName + "<font color=\"red\"> *</font>"
            viewBinding.statusText.text = if (viewData.status == "Pending") viewData.status else ""
            setStatusIcon(viewData.status)
        }

    }

    fun setStatusIcon(status: String){
        if (status == "Pending"){
            viewBinding.statusText.setTextColor(resources.getColor(R.color.pink_text))
            viewBinding.statusIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_check_pending))
        }else if (status == "Submitted"){
            viewBinding.statusIcon.setImageDrawable(resources.getDrawable(R.drawable.ic_pending_icon))
        }
    }
}