package com.gigforce.app.tl_work_space.home.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RelativeLayout
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentUpcomingGigersItemBinding
import com.gigforce.app.tl_work_space.home.TLWorkSpaceHomeUiEvents
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.core.IViewHolder
import com.google.android.material.card.MaterialCardView

class TLWorkspaceUpcomingGigersItemView(
    context: Context,
    attrs: AttributeSet?
) : MaterialCardView(
    context,
    attrs
), IViewHolder, View.OnClickListener {

    private lateinit var viewBinding: FragmentUpcomingGigersItemBinding
    private var viewData: TLWorkspaceRecyclerItemData.UpcomingGigerInnerItemData? = null

    init {
        elevation = resources.getDimension(R.dimen.card_elevation_mid)

        setDefault()
        inflate()
        setListenersOnView()
    }

    private fun setDefault() {
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
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
    }

    override fun bind(data: Any?) {
        (data as TLWorkspaceRecyclerItemData.UpcomingGigerInnerItemData?)?.let {

            viewBinding.userImageIv.loadProfilePicture(
                it.profilePictureThumbnail,
                it.profilePicture
            )
            viewBinding.gigerNameTextview.text = it.gigerName ?: ""
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
        viewData?.viewModel?.setEvent(
            TLWorkSpaceHomeUiEvents.UpcomingGigersSectionEvent.GigerClicked(
                viewData!!
            )
        )
    }

}