package com.gigforce.lead_management.ui.share_application_link

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.common_ui.views.GigforceToolbar
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PickJobProfileForReferralFragment : BaseFragment2<FragmentPickJobProfileForReferralBinding>(
    fragmentName = "PickJobProfileForReferralFragment",
    layoutId = R.layout.fragment_pick_job_profile_for_referral,
    statusBarColor = R.color.lipstick_2
) {
    @Inject lateinit var navigation: INavigation
    private val viewModel: ShareApplicationLinkViewModel by viewModels()

    //data
    private lateinit var userMobileNo : String
    private var joiningId : String? = null

    override fun viewCreated(
        viewBinding: FragmentPickJobProfileForReferralBinding,
        savedInstanceState: Bundle?
    ) {

        getDataFrom(
            arguments,
            savedInstanceState
        )
        initToolbar(viewBinding.toolbar)
        initView(viewBinding)
        initListeners(viewBinding)
        initViewModel()
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {
        arguments?.let {
            userMobileNo = it.getString(LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
        }

        savedInstanceState?.let {
            userMobileNo = it.getString(LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(
            LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER,
            userMobileNo
        )
        outState.putString(
            LeadManagementConstants.INTENT_EXTRA_JOINING_ID,
            joiningId
        )
    }


    private fun initToolbar(toolbar: GigforceToolbar) {
        toolbar.showTitle(getString(R.string.share_application_link_lead))
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


        lifecycleScope.launch {
            searchGigET.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect {
                    viewModel.searchJobProfiles(it)
                }
        }
    }

    private fun validateDataAndOpenReferralScreen() = viewBinding.apply {

        val selectedJobProfile = viewModel.getSelectedJobProfile()
        if (selectedJobProfile == null) {

            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.select_job_profile_lead))
                .setMessage(getString(R.string.select_atleast_one_profile_lead))
                .setPositiveButton(getString(R.string.okay_lead)) { _, _ -> }
                .show()

            return@apply
        }

        val userName = gigersNameET.text.toString().capitalize()
        if (userName.isEmpty()) {
            nameErrorTv.visible()
            nameErrorTv.text = getString(R.string.fill_user_name_lead)
            return@apply
        } else {
            nameErrorTv.gone()
        }


        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_REFERRAL,
            bundleOf(
                LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE to ShareReferralType.SHARE_SIGNUP_LINK,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID to selectedJobProfile.jobProfileId,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME to (selectedJobProfile.profileName ?: ""),
                LeadManagementConstants.INTENT_EXTRA_USER_NAME to userName,
                LeadManagementConstants.INTENT_EXTRA_PHONE_NUMBER to userMobileNo,
                LeadManagementConstants.INTENT_EXTRA_TRADE_NAME to (selectedJobProfile.tradeName ?: ""),
                LeadManagementConstants.INTENT_EXTRA_JOINING_ID to joiningId,
                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ICON to selectedJobProfile.companyLogo
            )
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

        gigsRecyclerView.collection = content.map {
            GigAppListRecyclerItemData.GigAppRecyclerItemData(
                status = "",
                jobProfileId = it.jobProfileId,
                tradeName = it.tradeName ?: getString(R.string.trade_name_lead),
                profileName = it.profileName ?: getString(R.string.profile_lead),
                companyLogo = it.companyLogo ?: "",
                it.ongoing,
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