package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationSearchItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class GigAppListRecyclerItemView  (
    context: Context,
    attrs: AttributeSet?
    ) : FrameLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {
    private var viewBinding: SelectGigApplicationItemLayoutBinding
    private var viewData: GigAppListRecyclerItemData.GigAppRecyclerItemData? = null



    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = SelectGigApplicationItemLayoutBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    override fun bind(data: Any?) {
        viewData = null

        data?.let {
            val gigApplicationData =
                it as GigAppListRecyclerItemData.GigAppRecyclerItemData
            viewData = gigApplicationData

            viewBinding.companyName.text = gigApplicationData.tradeName
            viewBinding.jobProfileTitle.text = gigApplicationData.profileName

            setBusinessLogo(
                gigApplicationData.companyLogo
            )
            setStatus(gigApplicationData.status)
            setViewSelected(gigApplicationData.selected)

            viewBinding.root.setOnClickListener {
//                if (gigApplicationData.selected){
//                    gigApplicationData.selected = false
//                    setViewSelected(gigApplicationData.selected)
//                } else {
//                    gigApplicationData.selected = true
//                    setViewSelected(gigApplicationData.selected)
//                }
                gigApplicationData.selectGigAppViewModel.selectJobProfile(gigApplicationData)
            }
           
        }
    }

    private fun setViewSelected(isSelected: Boolean){
        if (isSelected){
            viewBinding.root.background = resources.getDrawable(R.drawable.background_layout_selector)
        } else {
            viewBinding.root.background = resources.getDrawable(R.drawable.giger_profile_card_background)
        }
    }

    private fun setBusinessLogo(
        companyLogo: String) {

        Glide.with(context)
            .load(companyLogo)
            .placeholder(ShimmerHelper.getShimmerDrawable())
            .into(viewBinding.companyLogo)

    }

    private fun setStatus(
        status: String
    ) {
        if (status.isEmpty()) {
            viewBinding.status.gone()
        } else {
            viewBinding.status.visible()
            viewBinding.status.text = status
        }
    }

    override fun onClick(p0: View?) {

    }

}