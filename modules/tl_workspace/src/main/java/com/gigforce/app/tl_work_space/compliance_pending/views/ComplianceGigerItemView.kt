package com.gigforce.app.tl_work_space.compliance_pending.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.updateLayoutParams
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.compliance_pending.CompliancePendingFragmentViewEvents
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.databinding.FragmentUpcomingGigersItemBinding
import com.gigforce.app.tl_work_space.retentions.RetentionFragmentViewEvents
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.upcoming_gigers.UpcomingGigersViewContract
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
import com.gigforce.common_ui.utils.dp2Px
import com.gigforce.core.IViewHolder
import com.google.android.material.card.MaterialCardView

class ComplianceGigerItemView(
    context: Context,
    attrs: AttributeSet?
) : FrameLayout(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: FragmentUpcomingGigersItemBinding
    private var viewData: CompliancePendingScreenData.GigerItemData? = null

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setDefault() {
        val params = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )
        this.layoutParams = params
    }

    private fun inflate() {
        viewBinding = FragmentUpcomingGigersItemBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )
    }

    private fun setListenersOnView() {
        viewBinding.root.setOnClickListener(this)
        viewBinding.callGigerBtn.setOnClickListener(this)
    }

    override fun bind(data: Any?) {
        (data as CompliancePendingScreenData.GigerItemData?)?.let {
            viewData = it

            viewBinding.userImageIv.loadProfilePicture(
                it.profilePictureThumbnail,
                it.profilePicture
            )
            viewBinding.gigerNameTextview.text = it.gigerName
            viewBinding.gigerDesignationTextview.text = getCompanyDesignationString(
                it.business,
                it.jobProfile
            )
        }
    }

    private fun getCompanyDesignationString(business: String?, jobProfile: String?): String {
        return if (!business.isNullOrBlank() && !jobProfile.isNullOrBlank()) {
            "${business.capitalizeFirstLetter()}- ${jobProfile.capitalizeFirstLetter()}"
        } else if (!business.isNullOrBlank()) {
            business.capitalizeFirstLetter()
        } else if (!jobProfile.isNullOrBlank()) {
            jobProfile.capitalizeFirstLetter()
        } else {
            ""
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == R.id.call_giger_btn) {

            viewData?.viewModel?.setEvent(
                CompliancePendingFragmentViewEvents.CallGigerClicked(
                    viewData!!
                )
            )
        } else {

            viewData?.viewModel?.setEvent(
                CompliancePendingFragmentViewEvents.GigerClicked(
                    viewData!!
                )
            )
        }
    }

}