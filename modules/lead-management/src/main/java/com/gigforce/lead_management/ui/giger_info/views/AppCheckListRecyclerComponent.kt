package com.gigforce.lead_management.ui.giger_info.views

import android.content.Context
import android.os.Build
import android.text.Html
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.LayoutApplicationChecklistItemBinding
import com.gigforce.lead_management.models.ApplicationChecklistRecyclerItemData

class AppCheckListRecyclerComponent(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
) {

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

    fun bind(data: ApplicationChecklistRecyclerItemData.ApplicationChecklistItemData) {

        viewData = data
        if (viewData.isOptional) {
            viewBinding.checkListItemText.setText(viewData.checkName)
        } else {
            val txt = viewData.checkName + "<font color=\"red\"> *</font>"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                viewBinding.checkListItemText.setText(Html.fromHtml(txt,Html.FROM_HTML_MODE_LEGACY), TextView.BufferType.SPANNABLE)
            } else{
                viewBinding.checkListItemText.setText(Html.fromHtml(txt), TextView.BufferType.SPANNABLE)
            }
        }
        viewBinding.statusText.text = if (viewData.status == "Pending") context.getString(R.string.pending) else ""
        setStatusIcon(viewData.status)
    }

    private fun setStatusIcon(status: String) {
        if (status == "Pending") {
            viewBinding.statusDot.visible()
            viewBinding.statusText.setTextColor(ResourcesCompat.getColor(resources,R.color.pink_text,null))
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_check_pending,null))
        } else {
            viewBinding.statusDot.gone()
            viewBinding.statusIcon.setImageDrawable(ResourcesCompat.getDrawable(resources,R.drawable.ic_pink_tick,null))
        }
    }
}