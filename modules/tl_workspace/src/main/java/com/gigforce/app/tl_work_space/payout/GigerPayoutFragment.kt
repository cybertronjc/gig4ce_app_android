package com.gigforce.app.tl_work_space.payout

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.core.view.isVisible
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
import com.gigforce.app.tl_work_space.compliance_pending.CompliancePendingFragmentViewEvents
import com.gigforce.app.tl_work_space.compliance_pending.models.ComplianceStatusData
import com.gigforce.app.tl_work_space.custom_tab.CustomTabDataType1
import com.gigforce.app.tl_work_space.custom_tab.CustomTabDataType2
import com.gigforce.app.tl_work_space.databinding.GigerPayoutFragmentBinding
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutScreenData
import com.gigforce.app.tl_work_space.payout.models.GigerPayoutStatusData
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
class GigerPayoutFragment : BaseFragment2<GigerPayoutFragmentBinding>(
    fragmentName = TAG,
    layoutId = R.layout.giger_payout_fragment,
    statusBarColor = R.color.status_bar_pink
) {
    companion object {
        const val TAG = "GigerPayoutFragment"
    }

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: GigerPayoutViewModel by viewModels()

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
                        GigerPayoutFragmentViewEvents.FilterApplied.DateFilterApplied(selectedFilter)
                    )
                }
            }
        )
    }

    override fun viewCreated(
        viewBinding: GigerPayoutFragmentBinding,
        savedInstanceState: Bundle?
    ) {
        if (viewCreatedForTheFirstTime) {
            initView()
            observeViewStates()
            observeViewEffects()
        }
    }

    private fun initView() = viewBinding.apply{
        this.appBar.apply {
            setAppBarTitle(getToolBarTitleReceivedFromPreviousScreen() ?: "Payout")
            setBackButtonListener {

                if (isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                } else {
                    findNavController().navigateUp()
                }
            }
            filterImageButton.setOnClickListener {
                viewModel.setEvent(
                    GigerPayoutFragmentViewEvents.FilterApplied.OpenDateFilterDialog
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
                            GigerPayoutFragmentViewEvents.FilterApplied.SearchFilterApplied(
                                searchString
                            )
                        )
                    }
            }
        }

        this.payoutMainLayout.recyclerViewPayout.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setDiffUtilCallback(GigerPayoutListDiffUtil())
            setHasFixedSize(true)
        }

        this.swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(GigerPayoutFragmentViewEvents.RefreshGigerPayoutDataClicked)
        }
    }

    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState.collect{
            when(it) {
                is GigerPayoutFragmentUiState.ErrorWhileLoadingGigerPayoutData -> handleErrorLoadingInPayoutData(
                    it.error
                )

                is GigerPayoutFragmentUiState.LoadingGigerPayoutData -> handleLoadingState(
                    it.alreadyShowingGigersOnView
                )
                GigerPayoutFragmentUiState.ScreenInitialisedOrRestored -> {}
                is GigerPayoutFragmentUiState.ShowOrUpdateGigerPayoutData -> handleDataLoaded(
                    it.dateFilterSelected,
                    it.gigerPayoutData,
                    it.updatedTabMaster
                )
            }
        }
    }

    private fun handleDataLoaded(
        dateFilterSelected: TLWorkSpaceDateFilterOption?,
        gigerPayoutData: List<GigerPayoutScreenData>,
        updatedTabMaster: List<GigerPayoutStatusData>
    ) = viewBinding.apply{

        setSelectedDateFilterOnAppBar(dateFilterSelected)
        updateTabs(updatedTabMaster)

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        this.payoutMainLayout.recyclerViewPayout.collection = gigerPayoutData
        showOrHideNoDataLayout(
            gigerPayoutData.isNotEmpty()
        )
    }

    private fun showOrHideNoDataLayout(dataAvailableToShowOnScreen: Boolean) = viewBinding.payoutMainLayout.infoLayoutPayout.apply{
        if (dataAvailableToShowOnScreen) {
            root.gone()
            infoMessageTv.text = null
        } else {
            this.root.visible()
            this.infoMessageTv.text = "No gigers to show"
        }

    }

    private fun handleLoadingState(alreadyShowingGigersOnView: Boolean) = viewBinding.apply{
        this.payoutMainLayout.infoLayoutPayout.root.gone()

        if (alreadyShowingGigersOnView) {

            if (!swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = true
            }

            if (shimmerContainer.isVisible) {
                shimmerContainer.gone()
                stopShimmer(
                    this.shimmerContainer,
                    R.id.shimmer_controller
                )
            }
        } else {

            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }

            if (!shimmerContainer.isVisible) {
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

    }

    private fun handleErrorLoadingInPayoutData(error: String)  = viewBinding.apply {
        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        if (this.payoutMainLayout.recyclerViewPayout.childCount == 0) {

            payoutMainLayout.infoLayoutPayout.root.visible()
            payoutMainLayout.infoLayoutPayout.infoMessageTv.text = error
        } else {
            payoutMainLayout.infoLayoutPayout.root.gone()

            Snackbar.make(
                rootFrameLayout,
                error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated{
        viewModel.effect.collect{
            when(it) {

                is GigerPayoutFragmentViewUiEffects.DialogPhoneNumber -> {}

                is GigerPayoutFragmentViewUiEffects.OpenGigerDetailsBottomSheet -> openGigerDetailScreen(
                    it.gigerDetails
                )
                is GigerPayoutFragmentViewUiEffects.ShowDateFilterBottomSheet -> showDateFilterBottomSheet(
                    it.filters
                )
                is GigerPayoutFragmentViewUiEffects.ShowSnackBar -> showSnackBar(
                    it.message
                )

            }
        }

    }

    private fun showDateFilterBottomSheet(filters: List<TLWorkSpaceDateFilterOption>) {
        tlWorkSpaceNavigation.openFilterBottomSheet(
            filters
        )
    }

    private fun openGigerDetailScreen(gigerDetails: GigerPayoutScreenData.GigerItemData) {
        tlWorkSpaceNavigation.openGigerInfoBottomSheetForRetention(
            gigerDetails.gigerId
        )

    }

    private fun setSelectedDateFilterOnAppBar(
        selectedDateDateFilter: TLWorkSpaceDateFilterOption?
    ) {
        val dateFilter = selectedDateDateFilter ?: return
        viewBinding.appBar.setSubTitle(dateFilter.getFilterString())
    }

    private fun updateTabs(
        updatedTabMaster: List<GigerPayoutStatusData>
    ) = viewBinding.payoutMainLayout.tabRecyclerView.apply {
        if (updatedTabMaster.isEmpty()) return@apply

        if (layoutManager == null) {
            layoutManager = GridLayoutManager(
                requireContext(),
                3
            )
        }

        val tabsList = updatedTabMaster.map {
            CustomTabDataType1(
                id = it.id,
                title = it.title,
                value = it.value,
                selected = it.selected,
                valueChangedBy = it.countChangedBy,
                changeType = it.changeType,
                tabClickListener = it.viewModel
            )
        }
        collection = tabsList
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