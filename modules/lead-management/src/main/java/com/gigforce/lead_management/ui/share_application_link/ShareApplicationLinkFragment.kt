package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ShareApplicationLinkFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ShareApplicationLinkFragment : BaseFragment2<ShareApplicationLinkFragmentBinding>(
    fragmentName = "ShareApplicationLinkFragment",
    layoutId = R.layout.share_application_link_fragment,
    statusBarColor = R.color.colorAccent
) {
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    override fun viewCreated(
        viewBinding: ShareApplicationLinkFragmentBinding,
        savedInstanceState: Bundle?
    ) {


        initViewModel()
    }

    private fun initViewModel() {
        viewModel.viewState
            .observe(viewLifecycleOwner,{
                val jobProfiles = it ?: return@observe

                when (jobProfiles) {
                    is Lce.Content -> showJobProfiles(jobProfiles.content)
                    is Lce.Error -> showErrorInLoadingJobProfiles(jobProfiles.error)
                    Lce.Loading -> {
                        showJobProfilesAsLoading()
                    }
                }
            })
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