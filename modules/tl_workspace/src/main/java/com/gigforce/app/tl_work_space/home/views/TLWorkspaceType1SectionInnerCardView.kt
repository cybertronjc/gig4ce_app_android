package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.core.view.marginLeft
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionType1InnerItemBinding
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeUiEvents
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class TLWorkspaceType1SectionInnerCardView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private var viewData : TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData? = null
    private lateinit var viewBinding: RecyclerViewSectionType1InnerItemBinding

    init {
        setDefault()
        inflate(context)
        viewBinding.root.setOnClickListener(this)
    }

    private fun inflate(context: Context) {
        viewBinding = RecyclerViewSectionType1InnerItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        this.layoutParams = params
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkType1CardInnerItemData?)?.apply {
            viewData = this

            viewBinding.titleTextview.text = title.capitalizeFirstLetter()
            viewBinding.textView.text = value.toString()

            if (changeType == ValueChangeType.UNCHANGED) {
                viewBinding.valueRiseDipIndicator.gone()
            } else if (changeType == ValueChangeType.INCREMENT) {
                viewBinding.valueRiseDipIndicator.visible()
                viewBinding.valueRiseDipIndicator.showTextWithRiseIndicator(
                    valueChangedBy.toString()
                )
            } else {
                viewBinding.valueRiseDipIndicator.visible()
                viewBinding.valueRiseDipIndicator.showTextWithDipIndicator(
                    valueChangedBy.toString()
                )
            }
        }
    }

    override fun onClick(v: View?) {
        viewData?.viewModel?.setEvent(
            TLWorkSpaceHomeUiEvents.SectionType1Event.InnerCardClicked(
                viewData!!
            )
        )
    }

}