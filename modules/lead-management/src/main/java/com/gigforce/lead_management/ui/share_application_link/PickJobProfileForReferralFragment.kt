package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentPickJobProfileForReferralBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PickJobProfileForReferralFragment : BaseFragment2<FragmentPickJobProfileForReferralBinding>(
    fragmentName = "PickJobProfileForReferralFragment",
    layoutId = R.layout.fragment_pick_job_profile_for_referral,
    statusBarColor = R.color.lipstick_2
) {
    @Inject lateinit var navigation: INavigation
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    override fun viewCreated(
        viewBinding: FragmentPickJobProfileForReferralBinding,
        savedInstanceState: Bundle?
    ) {

        initToolbar(viewBinding.toolbar)
        initListeners(viewBinding)
        initViewModel()
    }

    private fun initToolbar(toolbar: GigforceToolbar) {
        toolbar.showTitle("Share Application Link")
        toolbar.hideActionMenu()
        toolbar.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun initListeners(
        viewBinding: FragmentPickJobProfileForReferralBinding
    ) = viewBinding.apply {

        PushDownAnim.setPushDownAnimTo(sendReferralLinkBtn).setOnClickListener {
            validateDataAndOpenReferralScreen()
        }
    }

    private fun validateDataAndOpenReferralScreen() = viewBinding.apply {

        val userName = gigersNameET.text.toString().capitalize()
        if (userName.isEmpty()) {
            nameErrorTv.visible()
            nameErrorTv.text = "Please fill user name"
            return@apply
        } else {
            nameErrorTv.gone()
        }

        val selectedJobProfile = viewModel.getSelectedJobProfile()
        if (selectedJobProfile == null) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Job Profile")
                .setMessage("Please select at least one job profile")
                .setPositiveButton("okay") { _, _ -> }
                .show()

            return@apply
        }

        logger.d(logTag,"navigating to $LeadManagementNavDestinations.FRAGMENT_REFERENCE_CHECK")
        navigation.navigateTo(
            dest = LeadManagementNavDestinations.FRAGMENT_REFERENCE_CHECK,
            args = bundleOf(
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID to selectedJobProfile.jobProfileId,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME to selectedJobProfile.profileName,
                LeadManagementConstants.INTENT_EXTRA_USER_NAME to userName,
            ),
            navOptions = getNavOptions()
        )
    }

    private fun initViewModel() {
        viewModel.viewState
            .observe(viewLifecycleOwner, {
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

    private fun showJobProfilesAsLoading() = viewBinding.apply {
        gigsRecyclerView.collection = emptyList()
        gigsListInfoLayout.root.gone()
        gigsShimmerContainer.visible()

        startShimmer(
            this.gigsShimmerContainer as LinearLayout,
            ShimmerDataModel(
                minHeight = R.dimen.size_120,
                minWidth = LinearLayout.LayoutParams.MATCH_PARENT,
                marginRight = R.dimen.size_16,
                marginTop = R.dimen.size_1,
                orientation = LinearLayout.VERTICAL
            ),
            R.id.shimmer_controller
        )
    }

    private fun showJobProfiles(
        content: List<JobProfileOverview>
    ) = viewBinding.apply {
        stopShimmer(
            gigsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigsShimmerContainer.gone()
        gigsListInfoLayout.root.gone()

        gigsRecyclerView.collection = content
    }

    private fun showErrorInLoadingJobProfiles(
        error: String
    ) = viewBinding.apply {
        gigsRecyclerView.collection = emptyList()
        stopShimmer(
            gigsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        gigsShimmerContainer.gone()
        gigsListInfoLayout.root.visible()

        gigsListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        gigsListInfoLayout.infoMessageTv.text = error
    }
}