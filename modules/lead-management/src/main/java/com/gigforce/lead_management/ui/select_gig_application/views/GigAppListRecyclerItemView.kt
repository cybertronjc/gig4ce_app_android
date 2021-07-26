package com.gigforce.lead_management.ui.select_gig_application.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationSearchItemLayoutBinding
import com.gigforce.lead_management.gigeronboarding.SelectGigApplicationToActivateViewModel
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.ui.share_application_link.ShareApplicationLinkViewModel


class GigAppListRecyclerItemView  (
    context: Context,
    attrs: AttributeSet?
    ) : FrameLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {

    private var viewBinding: SelectGigApplicationItemLayoutBinding
    private lateinit var viewData: GigAppListRecyclerItemData.GigAppRecyclerItemData
    private var shareApplicationLinkViewModel : ShareApplicationLinkViewModel? = null
    private var selectGigApplicationToActivateViewModel : SelectGigApplicationToActivateViewModel? = null


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
        viewBinding.rootCardView.setOnClickListener(this)
        viewBinding.root.setOnClickListener(this)
    }

    override fun bind(data: Any?) {

        data?.let {
            val gigApplicationData =
                it as GigAppListRecyclerItemData.GigAppRecyclerItemData
            viewData = gigApplicationData
            shareApplicationLinkViewModel = gigApplicationData.shareApplicationLinkViewModel
            selectGigApplicationToActivateViewModel = gigApplicationData.selectGigAppViewModel

            viewBinding.companyName.text = gigApplicationData.tradeName
            viewBinding.jobProfileTitle.text = gigApplicationData.profileName

            setBusinessLogo(
                gigApplicationData.companyLogo
            )
            setStatus(gigApplicationData.status)
            setViewSelected(gigApplicationData.selected)
        }
    }

    private fun setViewSelected(isSelected: Boolean){

        if (isSelected){
            viewBinding.root.background = ResourcesCompat.getDrawable(context.resources,R.drawable.background_layout_selector,null)
        } else {
            viewBinding.root.background = ResourcesCompat.getDrawable(context.resources,R.drawable.giger_profile_card_background,null)
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
        shareApplicationLinkViewModel?.selectJobProfile(
            viewData.jobProfileId
        )

        selectGigApplicationToActivateViewModel?.selectJobProfile(viewData)
    }

}