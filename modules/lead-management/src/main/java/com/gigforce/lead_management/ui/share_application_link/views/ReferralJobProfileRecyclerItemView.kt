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
import com.gigforce.lead_management.databinding.RecyclerRowReferralJobProfileLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class ReferralJobProfileRecyclerItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder,
    View.OnClickListener {

    private var viewBinding: RecyclerRowReferralJobProfileLayoutBinding
    private var viewData: GigAppListRecyclerItemData.GigAppRecyclerItemData? = null


    init {
        this.layoutParams =
            LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        viewBinding = RecyclerRowReferralJobProfileLayoutBinding.inflate(
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

        }
    }

    private fun setBusinessLogo(
        companyLogo: String
    ) {

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