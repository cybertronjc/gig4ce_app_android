package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.lead_management.LeadManagementConstants
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
    @Inject
    lateinit var navigation: INavigation
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    //Data
    private var shareType: String = ShareReferralType.SHARE_SIGNUP_LINK
    private var userUid: String? = null

    override fun viewCreated(
        viewBinding: FragmentPickJobProfileForReferralBinding,
        savedInstanceState: Bundle?
    ) {

        getArgumentsFrom(
            arguments,
            savedInstanceState
        )
        initToolbar(viewBinding.toolbar)
        initView()
        initListeners(viewBinding)
        initViewModel()
    }

    private fun initView() = viewBinding.apply {
        userNameMobileLayout.isVisible = shareType != ShareReferralType.SHARE_JOB_PROFILE_LINK
    }

    private fun getArgumentsFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            shareType = it.getString(LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE) ?: return@let
        }

        savedInstanceState?.let {
            shareType = it.getString(LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE) ?: return@let
        }

        logger.d(logTag, "shared type received from intents : '$shareType'")
    }

    private fun initToolbar(
        toolbar: GigforceToolbar
    ) {
        val shareTitle = if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {
            "Share Job Profile"
        } else {
            "Share Application Link"
        }

        toolbar.showTitle(shareTitle)
        toolbar.hideActionMenu()
        toolbar.setBackButtonListener {
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

        val selectedJobProfile = viewModel.getSelectedJobProfile()
        if (selectedJobProfile == null) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Select Job Profile")
                .setMessage("Please select at least one job profile")
                .setPositiveButton("okay") { _, _ -> }
                .show()

            return@apply
        }

        if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {

            viewModel.sendJobProfileReferralLink(
                userUid = userUid!!,
                jobProfileId = selectedJobProfile.jobProfileId,
                jobProfileName = selectedJobProfile.profileName ?: ""
            )
        } else {

            val userName = gigersNameET.text.toString().capitalize()
            if (userName.isEmpty()) {
                nameErrorTv.visible()
                nameErrorTv.text = "Please fill user name"
                return@apply
            } else {
                nameErrorTv.gone()
            }

            val userMobile = gigersMobileET.text.toString()
            if (userMobile.isEmpty()) {
                mobileErrorTv.visible()
                mobileErrorTv.text = "Please fill user mobile"
                return@apply
            } else {
                mobileErrorTv.gone()
            }

            viewModel.sendAppReferralLink(
                name = userName,
                mobileNumber = "+91$userMobile",
                jobProfileId = selectedJobProfile.jobProfileId,
                jobProfileName = selectedJobProfile.profileName ?: ""
            )
        }
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

        viewModel.referralViewState
            .observe(viewLifecycleOwner, {
                val referralState = it ?: return@observe

                when (referralState) {
                    ShareReferralViewState.DocumentUpdatedAndReferralShared -> {
                        UtilMethods.hideLoading()
                    }
                    is ShareReferralViewState.DocumentUpdatesButErrorInSharingDocument -> {
                        UtilMethods.hideLoading()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to share")
                            .setMessage("Unable to share, please share link through whatsapp")
                            .setPositiveButton("Open Whatsapp") { _, _ ->

                            }
                            .show()
                    }
                    is ShareReferralViewState.ErrorInCreatingOrUpdatingDocument -> {
                        UtilMethods.hideLoading()

                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to share")
                            .setMessage(referralState.error)
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                    ShareReferralViewState.SharingAndUpdatingJoiningDocument -> {
                        UtilMethods.showLongToast(requireContext(), "Sharing...")
                    }
                }
            })
    }

    private fun showJobProfilesAsLoading() = viewBinding.apply {
        gigsRecyclerView.collection = emptyList()
        gigsListInfoLayout.root.gone()
        gigsShimmerContainer.visible()

        startShimmer(
            this.gigsShimmerContainer,
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
            gigsShimmerContainer,
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
            gigsShimmerContainer,
            R.id.shimmer_controller
        )
        gigsShimmerContainer.gone()
        gigsListInfoLayout.root.visible()

        gigsListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        gigsListInfoLayout.infoMessageTv.text = error
    }
}