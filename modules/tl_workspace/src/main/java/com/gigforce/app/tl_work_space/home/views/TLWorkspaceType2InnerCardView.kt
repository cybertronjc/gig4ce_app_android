package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.updateLayoutParams
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.RecyclerViewSectionType2InnerItemBinding
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeUiEvents
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.dp
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
open class TLWorkspaceType2InnerCardView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
), IViewHolder, View.OnClickListener {
    private val viewBinding: RecyclerViewSectionType2InnerItemBinding
    private var viewData: TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData? = null

    init {
        elevation = resources.getDimension(R.dimen.size_0dp)
        strokeWidth = 1.dp
        strokeColor = ResourcesCompat.getColor(
            resources,
            R.color.lipstick_2,
            null
        )

        setDefault()
        viewBinding = RecyclerViewSectionType2InnerItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        viewBinding.root.setOnClickListener(this)
    }


    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(
                9.dp,
                0,
                9.dp,
                0
            )
        }

        this.layoutParams = params
    }

    override fun onClick(v: View?) {
        viewData?.viewModel?.setEvent(
            TLWorkSpaceHomeUiEvents.SectionType2Event.InnerCardClicked(
                viewData!!
            )
        )
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.TLWorkType2CardInnerItemData?)?.let {

            viewData = it

            viewBinding.labelTextview.text = it.title.capitalizeFirstLetter()
            viewBinding.countTextview.text = it.value.toString()
        }
    }

}