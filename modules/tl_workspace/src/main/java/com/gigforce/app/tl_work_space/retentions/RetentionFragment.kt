package com.gigforce.app.tl_work_space.retentions

import android.content.Intent
import android.net.Uri
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
import com.gigforce.app.tl_work_space.custom_tab.CustomTabDataType1
import com.gigforce.app.tl_work_space.databinding.FragmentRetentionBinding
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.app.tl_work_space.retentions.models.RetentionTabData
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
class RetentionFragment : BaseFragment2<FragmentRetentionBinding>(
    fragmentName = "RetentionFragment",
    layoutId = R.layout.fragment_retention,
    statusBarColor = R.color.status_bar_pink
) {
    companion object {
        const val TAG = "RetentionFragment"
    }

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: RetentionViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFragmentListenerForDateFilterSelection()
        getFilterFromPreviousScreenAndFetchData()
    }

    private fun getFilterFromPreviousScreenAndFetchData() {
        arguments?.let {

            val dateFilter: TLWorkSpaceDateFilterOption? = it.getParcelable(
                TLWorkSpaceNavigation.INTENT_EXTRA_DATE_FILTER
            )
            viewModel.refreshGigersData(dateFilter)
        }
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
                        RetentionFragmentViewEvents.FilterApplied.DateFilterApplied(selectedFilter)
                    )
                }
            }
        )
    }

    override fun viewCreated(
        viewBinding: FragmentRetentionBinding,
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
            setAppBarTitle(getToolBarTitleReceivedFromPreviousScreen() ?: "Retention")
            setBackButtonListener {

                if (isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                } else {
                    findNavController().navigateUp()
                }
            }
            filterImageButton.setOnClickListener {
                viewModel.setEvent(
                    RetentionFragmentViewEvents.OpenDateFilterIconClicked
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
                            RetentionFragmentViewEvents.FilterApplied.SearchFilterApplied(
                                searchString
                            )
                        )
                    }
            }
        }


        this.retentionMainLayout.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            itemAnimator = DefaultItemAnimator()
            setDiffUtilCallback(RetentionListDiffUtil())
            setHasFixedSize(true)
        }

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(RetentionFragmentViewEvents.RefreshRetentionDataClicked)
        }
        viewBinding.retentionMainLayout.infoLayout.infoIv.loadImage(
            R.drawable.ic_dragon_sleeping_animation
        )
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is RetentionFragmentViewUiEffects.DialogPhoneNumber -> dialPhoneNumber(
                        it.phoneNumber
                    )
                    is RetentionFragmentViewUiEffects.OpenGigerDetailsBottomSheet -> openGigerDetailsScreen(
                        it.gigerDetails
                    )
                    is RetentionFragmentViewUiEffects.ShowDateFilterBottomSheet -> showDateFilter(
                        it.dateFilters
                    )
                    is RetentionFragmentViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                }
            }
    }

    private fun showDateFilter(dateFilters: List<TLWorkSpaceDateFilterOption>) {
        tlWorkSpaceNavigation.openFilterBottomSheet(
            dateFilters
        )
    }

    private fun openGigerDetailsScreen(
        gigerDetails: RetentionScreenData.GigerItemData
    ) {
        tlWorkSpaceNavigation.openGigerInfoBottomSheetForRetention(
            gigerId = gigerDetails.gigerId,
            jobProfileId = gigerDetails.jobProfileId ?: "",//TODO fix this
            businessId = gigerDetails.businessId ?: ""
        )
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

    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                when (it) {
                    is RetentionFragmentUiState.ErrorWhileLoadingRetentionData -> handleErrorInLoadingData(
                        it.error
                    )
                    is RetentionFragmentUiState.LoadingRetentionData -> handleLoadingState(
                        it.alreadyShowingGigersOnView
                    )
                    RetentionFragmentUiState.ScreenInitialisedOrRestored -> {}
                    is RetentionFragmentUiState.ShowOrUpdateRetentionData -> handleDataLoadedState(
                        it.dateDateFilterSelected,
                        it.retentionData,
                        it.updatedTabMaster
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

        if (this.retentionMainLayout.recyclerView.childCount == 0) {

            retentionMainLayout.infoLayout.root.visible()
            retentionMainLayout.infoLayout.infoMessageTv.text = error
        } else {
            retentionMainLayout.infoLayout.root.gone()

            Snackbar.make(
                rootFrameLayout,
                error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun handleDataLoadedState(
        selectedDateDateFilter: TLWorkSpaceDateFilterOption?,
        retentionData: List<RetentionScreenData>,
        updatedTabMaster: List<RetentionTabData>
    ) = viewBinding.apply {

        setSelectedDateFilterOnAppBar(selectedDateDateFilter)
        updateTabs(updatedTabMaster)

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        this.retentionMainLayout.infoLayout.root.gone()
        this.retentionMainLayout.recyclerView.collection = retentionData
        showOrHideNoDataLayout(
            retentionData.isNotEmpty()
        )
    }

    private fun setSelectedDateFilterOnAppBar(
        selectedDateDateFilter: TLWorkSpaceDateFilterOption?
    ) {
        val dateFilter = selectedDateDateFilter ?: return
        viewBinding.appBar.setSubTitle(dateFilter.getFilterString())
    }

    private fun updateTabs(
        updatedTabMaster: List<RetentionTabData>
    ) = viewBinding.retentionMainLayout.tabRecyclerView.apply {
        if (updatedTabMaster.isEmpty()) return@apply
        logger.d(TAG, "received status from viewmodel : $updatedTabMaster")

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
                valueChangedBy = it.valueChangedBy,
                changeType = it.changeType,
                tabClickListener = it.viewModel
            )
        }
        collection = tabsList
    }

    private fun showOrHideNoDataLayout(
        dataAvailableToShowOnScreen: Boolean
    ) = viewBinding.retentionMainLayout.infoLayout.apply {

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
        this.retentionMainLayout.infoLayout.root.gone()

        if (anyPreviousDataShownOnScreen) {

            swipeRefreshLayout.isRefreshing = true

            shimmerContainer.gone()
            stopShimmer(
                this.shimmerContainer as LinearLayout,
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