package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.utils.UtilMethods
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningSignUpInitiatedMode
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
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
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
        initView(viewBinding)
        initListeners(viewBinding)
        initViewModel()
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

    private fun initToolbar(toolbar: GigforceToolbar) {
        val shareTitle = if (shareType == ShareReferralType.SHARE_JOB_PROFILE_LINK) {
            "Share Job Profile"
        } else {
            "Share Application Link"
        }

        toolbar.showTitle(shareTitle)
        toolbar.hideActionMenu()
        toolbar.setBackButtonListener{
            activity?.onBackPressed()
        }
    }

    private fun initView(
        viewBinding: FragmentPickJobProfileForReferralBinding
    )= viewBinding.apply {
        gigsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
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

            navigation.navigateTo(
                LeadManagementNavDestinations.FRAGMENT_REFERRAL,
                bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE to ShareReferralType.SHARE_JOB_PROFILE_LINK,
                    LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID to selectedJobProfile.jobProfileId,
                    LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME to (selectedJobProfile.profileName ?: ""),
                    LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid!!
                )
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


            navigation.navigateTo(
                LeadManagementNavDestinations.FRAGMENT_REFERRAL,
                bundleOf(
                    LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE to ShareReferralType.SHARE_SIGNUP_LINK,
                    LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID to selectedJobProfile.jobProfileId,
                    LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME to (selectedJobProfile.profileName ?: ""),
                    LeadManagementConstants.INTENT_EXTRA_USER_NAME to userName,
                    LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER to "+91$userMobile",
                )
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

        gigsRecyclerView.collection = content.map {
            GigAppListRecyclerItemData.GigAppRecyclerItemData(
                status = "",
                jobProfileId = it.jobProfileId,
                tradeName = it.tradeName ?: "Trade name N/A",
                profileName = it.profileName ?: "Profile N/A",
                companyLogo = it.companyLogo ?: "",
                selected = it.isSelected,
                selectGigAppViewModel = null,
                shareApplicationLinkViewModel = viewModel
            )
        }
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

        //gigsListInfoLayout.infoIv.loadImage(R.drawable.ic_no_joining_found)
        //gigsListInfoLayout.infoMessageTv.text = error
    }
}