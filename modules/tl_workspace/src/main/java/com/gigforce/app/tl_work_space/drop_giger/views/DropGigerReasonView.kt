package com.gigforce.app.tl_work_space.drop_giger.views

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentDropGigerMainItemViewBinding
import com.gigforce.app.tl_work_space.drop_giger.DropGigerFragmentViewEvents
import com.gigforce.app.tl_work_space.drop_giger.models.DropOption
import com.gigforce.core.IViewHolder
import com.google.android.material.card.MaterialCardView

class DropGigerReasonView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: FragmentDropGigerMainItemViewBinding
    private var viewData: DropOption? = null

    init {

        setDefault()
        inflate()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        val pxStartEnd = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            20f,
            resources.displayMetrics
        ).toInt()

        val pxTopBottom = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            10f,
            resources.displayMetrics
        ).toInt()

        params.setMargins(
            pxStartEnd,
            pxTopBottom,
            pxStartEnd,
            pxTopBottom
        )

        this.layoutParams = params

        elevation = resources.getDimension(R.dimen.size4)
        radius = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            6f,
            resources.displayMetrics
        )
    }

    private fun inflate() {
        viewBinding = FragmentDropGigerMainItemViewBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
        viewBinding.root.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as DropOption).apply {
            viewData = this
            viewBinding.reasonNameTv.text = this.dropLocalizedText

            if (this.selected) {

                viewBinding.reasonRootLayout.background = ContextCompat.getDrawable(
                    context,
                    R.drawable.background_card_selected
                )
                viewBinding.selectedIv.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_selected_tick,
                        null
                    )
                )
            } else {
                viewBinding.reasonRootLayout.setBackgroundResource(
                    R.drawable.background_card_no_effect
                )
                viewBinding.selectedIv.setImageDrawable(
                    ResourcesCompat.getDrawable(
                        context.resources,
                        R.drawable.ic_unselect_tick,
                        null
                    )
                )
            }
        }
    }

    override fun onClick(v: View?) {
        viewData?.viewModel?.setEvent(
            DropGigerFragmentViewEvents.ReasonSelected(
                viewData!!
            )
        )
    }
}