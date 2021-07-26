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
    private lateinit var assignGigRequest: AssignGigRequest

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
            //assignGigRequest = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_ID) ?: return@let
            //assignGigRequest = it.getParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL) ?: return@let
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

//        if (::assignGigRequest.isInitialized.not()) {
//            logger.e(
//                logTag,
//                "null assignGigRequest received from bundles",
//                Exception("null assignGigRequest received from bundles")
//            )
//        } else {
//            logger.d(logTag, "AssignGigRequest received from bundles : $assignGigRequest")
//        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_ID, userUid)
        //outState.putParcelable(LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL, assignGigRequest)
    }

    private fun initViews() {
//        viewBinding.gigerProfileCard.apply {
//            setProfileCard(GigerProfileCardDVM("https://instagram.fdel11-2.fna.fbcdn.net/v/t51.2885-19/s320x320/125221466_394003705121691_8790543636526463384_n.jpg", "Jagdish Choudhary", "+919898833257", "Swiggy delivery", ""))
//        }
    }

    private fun initViewModel() = viewBinding.apply {
        viewModel.getJobProfilesToActivate("d5ToQmOn6sdAcPWvjsBuhYWm9kF3")
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: ""

            when (state) {
                is SelectGigAppViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingGigApps(
                    state.error
                )
                is SelectGigAppViewState.GigAppListLoaded -> showGigApps(state.gigApps)
                SelectGigAppViewState.LoadingDataFromServer -> loadingGigAppsFromServer()
                SelectGigAppViewState.NoGigAppsFound -> showNoGigAppsFound()
            }
        })
        viewModel.selectedIndex.observe(viewLifecycleOwner, Observer {
            submitBtn.isEnabled = it == -1
        })
        viewModel.selectedJobProfileOverview.observe(viewLifecycleOwner, Observer {
            logger.d(TAG, "selected job profile $it")
            submitBtn.text = if (it.ongoing!!) "Share Referral Link" else "Next"
        })
    }

    private fun initListeners() {
        viewBinding.submitBtn.setOnClickListener {
            if (viewModel.getSelectedIndex() != -1) {
                viewModel.getSelectedJobProfile().let {
                    assignGigRequest.jobProfileId = it.jobProfileId
                    assignGigRequest.jobProfileName = it.profileName.toString()

                    //logger.d(TAG, "Company logo: ${assignGigRequest.companyLogo}")
                    navigation.navigateTo(
                        LeadManagementNavDestinations.FRAGMENT_SELECT_GIG_LOCATION, bundleOf(
                            LeadManagementConstants.INTENT_EXTRA_USER_ID to userUid,
                            LeadManagementConstants.INTENT_EXTRA_ASSIGN_GIG_REQUEST_MODEL to assignGigRequest
                        )
                    )
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
        
        viewLifecycleOwner.lifecycleScope.launch {
            viewBinding.gigerProfileCard.setGigerProfileData(userUid)
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