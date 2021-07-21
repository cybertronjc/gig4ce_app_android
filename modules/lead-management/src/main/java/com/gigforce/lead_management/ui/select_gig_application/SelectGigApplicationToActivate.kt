package com.gigforce.lead_management.ui.select_gig_application

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewdatamodels.GigerProfileCardDVM
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.LeadManagementConstants
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.ReferenceCheckFragmentBinding
import com.gigforce.lead_management.databinding.SelectGigApplicationToActivateFragmentBinding
import com.gigforce.lead_management.gigeronboarding.SelectGigAppViewState
import com.gigforce.lead_management.gigeronboarding.SelectGigApplicationToActivateViewModel
import com.gigforce.lead_management.models.GigAppListRecyclerItemData
import dagger.hilt.android.AndroidEntryPoint
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
        private var isNumberRegistered = false
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SelectGigApplicationToActivateViewModel by viewModels()
    private  var userUid: String = ""

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
            activity?.onBackPressed()
        }
    }

    private fun getDataFrom(
        arguments: Bundle?,
        savedInstanceState: Bundle?
    ) {

        arguments?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
        }

        savedInstanceState?.let {
            userUid = it.getString(LeadManagementConstants.INTENT_EXTRA_USER_UID) ?: return@let
        }
    }

    override fun onSaveInstanceState(
        outState: Bundle
    ) {
        super.onSaveInstanceState(outState)
        outState.putString(LeadManagementConstants.INTENT_EXTRA_USER_UID, userUid)
    }

    private fun initViews() {
        viewBinding.gigerProfileCard.apply {
            setProfileCard(GigerProfileCardDVM("https://instagram.fdel11-2.fna.fbcdn.net/v/t51.2885-19/s320x320/125221466_394003705121691_8790543636526463384_n.jpg", "Jagdish Choudhary", "+919898833257", "Swiggy delivery", ""))
        }
    }

    private fun initViewModel() {
        viewModel.fetchGigApplications(userUid)
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            val state = it ?: ""

            when (state) {
                is SelectGigAppViewState.ErrorInLoadingDataFromServer -> showErrorInLoadingGigApps(state.error)
                is SelectGigAppViewState.GigAppListLoaded -> showGigApps(state.gigApps)
                SelectGigAppViewState.LoadingDataFromServer -> loadingGigAppsFromServer()
                SelectGigAppViewState.NoGigAppsFound -> showNoGigAppsFound()
            }
        })
    }

    private fun initListeners() {

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
    ) = viewBinding.apply{
        stopShimmer(viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.gone()

        viewBinding.gigApplicationsRv.collection = gigAppList
    }

    private fun showNoGigAppsFound() = viewBinding.apply{

        viewBinding.gigApplicationsRv.collection = emptyList()
        stopShimmer(viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.visible()

        //todo show illus here
    }

    private fun showErrorInLoadingGigApps(
        error: String
    ) = viewBinding.apply{

        viewBinding.gigApplicationsRv.collection = emptyList()
        stopShimmer(viewBinding.gigappsShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )
        viewBinding.gigappsShimmerContainer.gone()
        viewBinding.gigappListInfoLayout.root.visible()

    }

}