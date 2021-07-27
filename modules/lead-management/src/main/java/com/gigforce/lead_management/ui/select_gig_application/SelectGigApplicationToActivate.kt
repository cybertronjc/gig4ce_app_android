package com.gigforce.lead_management.ui.select_gig_application

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.common_ui.viewdatamodels.leadManagement.AssignGigRequest
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.LeadManagementNavDestinations
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.SelectGigApplicationToActivateFragmentBinding
import com.gigforce.lead_management.gigeronboarding.SelectGigAppViewState
import com.gigforce.lead_management.gigeronboarding.SelectGigApplicationToActivateViewModel
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import com.gigforce.lead_management.ui.share_application_link.ShareReferralType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class SelectGigApplicationToActivate : BaseFragment2<SelectGigApplicationToActivateFragmentBinding>(
    fragmentName = "SelectGigApplicationFragment",
    layoutId = R.layout.select_gig_application_to_activate_fragment,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = SelectGigApplicationToActivate()
        private const val TAG = "SelectGigApplicationFragment"
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: SelectGigApplicationToActivateViewModel by viewModels()

    private lateinit var userUid: String
    private var joiningId: String? = null

    override fun viewCreated(
        viewBinding: SelectGigApplicationToActivateFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        initToolbar(viewBinding)
        getDataFrom(
            arguments,
            savedInstanceState
        )
        initViews()
        initListeners()
        initViewModel()
    }

    private fun initToolbar(
        viewBinding: SelectGigApplicationToActivateFragmentBinding
    ) = viewBinding.toolbar.apply {

        showTitle("Gig Application")
        hideSearchOption()
        setBackButtonListener {
            navigation.popBackStack()
        }
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            joiningId = it.getString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID) ?: return@let
        }
        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {
        if (::userUid.isInitialized) {
            logger.d(logTag, "User-id received from bundles : $userUid")
        } else {
            logger.e(
                logTag,
                "no User-id received from bundles",
                Exception("no User-id received from bundles")
            )
        }

        if (joiningId != null) {
            logger.d(logTag, "joiningId received from bundles : $joiningId")
        } else {
            logger.e(
                logTag,
                "no joiningId received from bundles",
                Exception("no joiningId received from bundles")
            )
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_JOINING_ID, joiningId)
    }

    private fun initViews() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewBinding.gigerProfileCard.setGigerProfileData(userUid)
        }
    }

    private fun initViewModel() = viewBinding.apply {
        viewModel.getJobProfilesToActivate(userUid)
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: return@Observer
            when (state) {
                is SelectGigAppViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingGigApps(
                    state.error
                )
                is SelectGigAppViewState.GigAppListLoaded -> showGigApps(state.gigApps)
                SelectGigAppViewState.LoadingDataFromServer -> loadingGigAppsFromServer()
                SelectGigAppViewState.NoGigAppsFound -> showNoGigAppsFound()
                is SelectGigAppViewState.StartGigerJoiningProcess -> {

                    navigation.navigateTo(

                        LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_LOCATION, bundleOf(
                            LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to state.assignGigRequest,
                            LeadManagementConstants.INTENT_EXTRA_JOINING_ID to joiningId,
                            LeadManagementConstants.INTENT_EXTRA_CURRENT_JOINING_USER_INFO to state.gigerInfo
                        )
                    )
                }
                is SelectGigAppViewState.ErrorInStartingJoiningProcess -> {

                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Unble to start Joining")
                        .setMessage(state.error)
                        .setPositiveButton("Okay") { _, _ -> }
                        .show()
                }
                SelectGigAppViewState.FetchingDataToStartJoiningProcess -> {

                }
            }
        })

        viewModel.selectedIndex.observe(viewLifecycleOwner, Observer {
            submitBtn.isEnabled = it == -1
        })

        viewModel.selectedJobProfileOverview.observe(viewLifecycleOwner, Observer {
            logger.d(TAG, "selected job profile $it")
            submitBtn.text = if (it.ongoing) "Share Referral Link" else "Next"
        })


    }

    private fun initListeners() {
        viewBinding.submitBtn.setOnClickListener {
            if (viewModel.getSelectedIndex() != -1) {
                viewModel.getSelectedJobProfile().let {


                    //todo Check Application submitted flag
                    if (!it.ongoing) {

                        if("Application submitted".equals(it.status,true)) {
                            viewModel.fetchInfoAndStartJoiningProcess(
                                userUid = userUid,
                                joiningId = joiningId,
                                jobProfileOverview = it
                            )
                        }
                    } else {

                        navigation.navigateTo(
                            LeadManagementNavDestinations.FRAGMENT_REFERRAL, bundleOf(
                                LeadManagementConstants.INTENT_EXTRA_SHARE_TYPE to ShareReferralType.SHARE_JOB_PROFILE_LINK,
                                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_ID to it.jobProfileId,
                                LeadManagementConstants.INTENT_EXTRA_JOB_PROFILE_NAME to (it.profileName
                                    ?: ""),
                                LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                                LeadManagementConstants.INTENT_EXTRA_TRADE_NAME to it.tradeName
                            )
                        )

                    }
                }
            }
        }

        viewBinding.toolbar.apply {
            hideActionMenu()
            showTitle("Gig Application")
            setBackButtonListener(View.OnClickListener {
                navigation.popBackStack()
            })
        }
    }

    private fun loadingGigAppsFromServer() = viewBinding.apply {

        viewBinding.gigApplicationsRv.collection = emptyList()
        viewBinding.gigappListInfoLayout.root.gone()
        viewBinding.gigappsShimmerContainer.visible()

        startShimmer(
            this.gigappsShimmerContainer as LinearLayout,
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

    private fun showGigApps(
        gigAppList: List<GigAppListRecyclerItemData>
    ) = viewBinding.apply {
        stopShimmer(
            viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.gone()

        viewBinding.gigApplicationsRv.collection = gigAppList
    }

    private fun showNoGigAppsFound() = viewBinding.apply {

        viewBinding.gigApplicationsRv.collection = emptyList()
        stopShimmer(
            viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.visible()

        //todo show illus here
    }

    private fun showErrorInLoadingGigApps(
        error: String
    ) = viewBinding.apply {

        viewBinding.gigApplicationsRv.collection = emptyList()
        stopShimmer(
            viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.visible()
    }

}