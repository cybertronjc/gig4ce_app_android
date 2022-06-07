package com.gigforce.app.tl_work_space.drop_giger

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.compliance_pending.models.CompliancePendingScreenData
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData
import com.gigforce.app.tl_work_space.custom_tab.CustomTabDataType2
import com.gigforce.app.tl_work_space.databinding.FragmentCompliancePendingBinding
import com.gigforce.app.tl_work_space.databinding.FragmentDropGigerBinding
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class DropGigerFragment : BaseFragment2<FragmentDropGigerBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_drop_giger,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "DropGigerFragment"
    }

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: CompliancePendingViewModel by viewModels()
    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentListenerForDateFilterSelection()
    }

    private fun setFragmentListenerForDateFilterSelection() {

        setFragmentResultListener(
            TLWorkSpaceNavigation.FRAGMENT_RESULT_KEY_DATE_FILTER,
            listener = { requestKey: String, bundle: Bundle ->

                if (TLWorkSpaceNavigation.FRAGMENT_RESULT_KEY_DATE_FILTER == requestKey) {
                    val selectedFilter =
                        TLWorkSpaceNavigation.FragmentResultHandler.getDateFilterResult(
                            bundle
                        ) ?: return@setFragmentResultListener

                    viewModel.setEvent(
                        CompliancePendingFragmentViewEvents.FilterApplied.DateFilterApplied(
                            selectedFilter
                        )
                    )
                }
            }
        )
    }

    override fun viewCreated(
        viewBinding: FragmentDropGigerBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {

            initView()
            observeViewStates()
            observeViewEffects()
        }
    }


    private fun initView() = viewBinding.apply {


        appBar.apply {
            setAppBarTitle(getToolBarTitleReceivedFromPreviousScreen() ?: "Pending Compliance")
            setBackButtonListener {

                if (isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                } else {
                    findNavController().navigateUp()
                }
            }
            filterImageButton.setOnClickListener {
                viewModel.setEvent(
                    CompliancePendingFragmentViewEvents.FilterApplied.OpenDateFilterDialog
                )
            }

            changeBackButtonDrawable()
            lifecycleScope.launchWhenCreated {

                search_item.getTextChangeAsStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->

                        Log.d("Search ", "Searhcingg...$searchString")
                        viewModel.setEvent(
                            CompliancePendingFragmentViewEvents.FilterApplied.SearchFilterApplied(
                                searchString
                            )
                        )
                    }
            }
        }


        this.mainLayout.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setDiffUtilCallback(CompliancePendingListDiffUtil())
            setHasFixedSize(true)
        }

        swipeRefreshLayout.setOnRefreshListener {

            viewModel.setEvent(
                CompliancePendingFragmentViewEvents.RefreshDataClicked
            )
        }

        viewBinding.mainLayout.infoLayout.infoIv.loadImage(
            R.drawable.ic_dragon_sleeping_animation
        )
    }

    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                when (it) {
                    is CompliancePendingFragmentUiState.ErrorWhileLoadingData -> handleErrorInLoadingData(
                        it.error
                    )
                    is CompliancePendingFragmentUiState.LoadingData -> handleLoadingState(
                        it.alreadyShowingGigersOnView
                    )
                    CompliancePendingFragmentUiState.ScreenInitialisedOrRestored -> {}
                    is CompliancePendingFragmentUiState.ShowOrUpdateData -> handleDataLoadedState(
                        selectedDateDateFilter = it.dateDateFilterSelected,
                        complianceData = it.complianceData,
                        updatedTabMaster = it.tabData
                    )
                }
            }
    }

    private fun handleErrorInLoadingData(
        error: String
    ) = viewBinding.apply {
        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        if (this.mainLayout.recyclerView.childCount == 0) {

            mainLayout.infoLayout.root.visible()
            mainLayout.infoLayout.infoMessageTv.text = error
        } else {
            mainLayout.infoLayout.root.gone()

            Snackbar.make(
                rootFrameLayout,
                error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleDataLoadedState(
        selectedDateDateFilter: TLWorkSpaceDateFilterOption?,
        complianceData: List<CompliancePendingScreenData>,
        updatedTabMaster: List<ComplianceStatusData>
    ) = viewBinding.apply {

        setSelectedDateFilterOnAppBar(selectedDateDateFilter)
        updateTabs(updatedTabMaster)

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        this.mainLayout.infoLayout.root.gone()
        this.mainLayout.recyclerView.collection = complianceData
        showOrHideNoDataLayout(
            complianceData.isNotEmpty()
        )
    }

    private fun setSelectedDateFilterOnAppBar(
        selectedDateDateFilter: TLWorkSpaceDateFilterOption?
    ) {
        val dateFilter = selectedDateDateFilter ?: return
        viewBinding.appBar.setSubTitle(dateFilter.getFilterString())
    }

    private fun updateTabs(
        updatedTabMaster: List<ComplianceStatusData>
    ) = viewBinding.mainLayout.tabRecyclerView.apply {
        if (updatedTabMaster.isEmpty()) return@apply

        if (layoutManager == null) {
            layoutManager = GridLayoutManager(
                requireContext(),
                3
            )
        }

        val tabsList = updatedTabMaster.map {
            CustomTabDataType2(
                id = it.id,
                title = it.title,
                value = it.value,
                selected = it.selected,
                tabClickListener = it.viewModel
            )
        }
        collection = tabsList
    }

    private fun showOrHideNoDataLayout(
        dataAvailableToShowOnScreen: Boolean
    ) = viewBinding.mainLayout.infoLayout.apply {

        if (dataAvailableToShowOnScreen) {
            root.gone()
            infoMessageTv.text = null
        } else {
            this.root.visible()
            this.infoMessageTv.text = "No gigers to show"
        }
    }

    private fun handleLoadingState(
        anyPreviousDataShownOnScreen: Boolean
    ) = viewBinding.apply {
        this.mainLayout.infoLayout.root.gone()

        if (anyPreviousDataShownOnScreen) {
            swipeRefreshLayout.isRefreshing = true

            shimmerContainer.gone()
            stopShimmer(
                this.shimmerContainer,
                R.id.shimmer_controller
            )
        } else {
            swipeRefreshLayout.isRefreshing = false
            startShimmer(
                this.shimmerContainer as LinearLayout,
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
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is CompliancePendingViewUiEffects.DialogPhoneNumber -> dialPhoneNumber(
                        it.phoneNumber
                    )
                    is CompliancePendingViewUiEffects.OpenGigerDetailsBottomSheet -> openGigerDetailsScreen(
                        it.gigerDetails
                    )
                    is CompliancePendingViewUiEffects.ShowDateFilterBottomSheet -> showDateFilter(
                        it.dateFilters
                    )
                    is CompliancePendingViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                }
            }
    }

    private fun dialPhoneNumber(
        phoneNumber: String
    ) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phoneNumber, null))
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun openGigerDetailsScreen(
        gigerDetails: CompliancePendingScreenData.GigerItemData
    ) {
        tlWorkSpaceNavigation.openGigerInfoBottomSheetForCompliance(
            gigerId = gigerDetails.gigerId,
            jobProfileId = gigerDetails.jobProfileId!!,
            businessId = gigerDetails.business!!,
            eJoiningId = null
        )
    }

    private fun showDateFilter(dateFilters: List<TLWorkSpaceDateFilterOption>) {
        tlWorkSpaceNavigation.openFilterBottomSheet(
            dateFilters
        )
    }

    private fun showSnackBar(
        message: String
    ) {
        Snackbar.make(
            viewBinding.rootFrameLayout,
            message,
            Snackbar.LENGTH_SHORT
        ).show()
    }

}