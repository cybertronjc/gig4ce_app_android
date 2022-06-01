package com.gigforce.app.tl_work_space.upcoming_gigers.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentUpcomingGigersBusinessItemBinding
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.upcoming_gigers.UpcomingGigersViewContract
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
import com.gigforce.core.IViewHolder
import com.google.android.material.card.MaterialCardView

class UpcomingGigersBusinessItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder {

    private lateinit var viewBinding: FragmentUpcomingGigersBusinessItemBinding

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
        viewBinding = FragmentUpcomingGigersBusinessItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        (data as UpcomingGigersListData.BusinessItemData).let {

            viewBinding.textview.text = it.businessName
        }
    }
}