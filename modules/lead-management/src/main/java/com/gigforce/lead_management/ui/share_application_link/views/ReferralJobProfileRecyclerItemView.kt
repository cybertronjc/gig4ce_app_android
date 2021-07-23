package com.gigforce.lead_management.ui.share_application_link.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.bumptech.glide.Glide
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class ReferralJobProfileRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private var viewBinding: SelectGigApplicationItemLayoutBinding
    private var viewData: GigAppListRecyclerItemData.GigAppRecyclerItemData? = null


    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
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

            viewBinding.companyName.text = gigApplicationData.businessName
            viewBinding.jobProfileTitle.text = gigApplicationData.jobProfileTitle

            setBusinessLogo(
                gigApplicationData.businessLogoThumbnail,
                gigApplicationData.businessLogo
            )
            setStatus(gigApplicationData.status)

        }
    }

    private fun setBusinessLogo(
        businessLogoThumbnail: String,
        businessLogo: String
    ) {

        Glide.with(context)
            .load(businessLogo)
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