package com.gigforce.app.tl_work_space.custom_tab

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.CustomTabItemBinding
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible

open class TabWithCountAndChangeInfoCardView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private var viewData: CustomTabData? = null
    private lateinit var viewBinding: CustomTabItemBinding

    init {
        //setDefault()
        inflate(context)
        viewBinding.root.setOnClickListener(this)
    }

    private fun inflate(context: Context) {
        viewBinding = CustomTabItemBinding.inflate(
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
        (data as CustomTabData).apply {
            viewData = this

            viewBinding.titleTextview.text = title.capitalizeFirstLetter()
            viewBinding.textView.text = value.toString()

            if (this.selected) {
                viewBinding.rootConstraintLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.cardview_selected_pink_border
                )
            } else {
                viewBinding.rootConstraintLayout.setBackgroundResource(R.drawable.cardview_not_selected)
            }

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
        viewData?.viewModel?.handleCustomTabClick(viewData!!)
    }

}