package com.gigforce.lead_management.gigeronboarding.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.IViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.lead_management.databinding.SelectGigApplicationItemLayoutBinding
import com.gigforce.lead_management.models.GigAppListRecyclerItemData


class GigAppListRecyclerItemView  (
    context: Context,
    attrs: AttributeSet?
    ) : RelativeLayout(
    context,
    attrs
    ), IViewHolder, View.OnClickListener {
    private lateinit var viewBinding: SelectGigApplicationItemLayoutBinding
    private var viewData: GigAppListRecyclerItemData.GigAppRecyclerItemData? = null



    init {
        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setListenersOnView() {

    }

    private fun setDefault() {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
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
        businessLogo: String) {

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