package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.base.BaseFragment2
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentLeadManagementReferralBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareApplicationLinkFragment : BaseFragment2<FragmentLeadManagementReferralBinding>(
    fragmentName = "ShareApplicationLinkFragment",
    layoutId = R.layout.fragment_lead_management_referral,
    statusBarColor = R.color.colorAccent
) {
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    override fun viewCreated(
        viewBinding: FragmentLeadManagementReferralBinding,
        savedInstanceState: Bundle?
    ) {


        initListeners(viewBinding)
        initViewModel()
    }

    private fun initListeners(
        viewBinding: FragmentLeadManagementReferralBinding
    ) = viewBinding.apply {

        PushDownAnim
            .setPushDownAnimTo(sendReferralLinkBtn)
            .setOnClickListener {



            }
    }

    private fun initViewModel() {

    }

    private fun showJobProfilesAsLoading() {

    }

    private fun showJobProfiles(
        content: List<JobProfileOverview>
    ) {

    }

    private fun showErrorInLoadingJobProfiles(
        error: String
    ) {

    }
}