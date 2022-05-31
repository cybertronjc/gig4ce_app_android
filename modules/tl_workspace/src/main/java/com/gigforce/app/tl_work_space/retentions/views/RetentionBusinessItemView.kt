package com.gigforce.app.tl_work_space.retentions.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentRetentionMainBusinessItemBinding
import com.gigforce.app.tl_work_space.retentions.RetentionFragmentViewEvents
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.core.IViewHolder

class RetentionBusinessItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: FragmentRetentionMainBusinessItemBinding
    private var viewData: RetentionScreenData.BusinessItemData? = null

    init {

        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params

    }

    private fun inflate() {
        viewBinding = FragmentRetentionMainBusinessItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.root.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as RetentionScreenData.BusinessItemData?)?.let {
            viewData = it

            if (it.expanded) {
                viewBinding.companyNameTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.pink_text, null)
                )
                viewBinding.collapseButton.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_up, null)
                )
            } else {
                viewBinding.companyNameTv.setTextColor(
                    ResourcesCompat.getColor(resources, R.color.text_grey, null)
                )
                viewBinding.collapseButton.setImageDrawable(
                    ResourcesCompat.getDrawable(resources, R.drawable.ic_dropdown_drop, null)
                )
            }

            viewBinding.companyNameTv.text = "${it.businessName} (${it.count})"
        }
    }

    override fun onClick(v: View?) {
        viewData?.viewModel?.setEvent(
            RetentionFragmentViewEvents.BusinessClicked(viewData!!)
        )
    }
}