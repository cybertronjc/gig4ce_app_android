package com.gigforce.app.tl_work_space.retentions

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentRetentionBinding
import com.gigforce.app.tl_work_space.retentions.models.RetentionScreenData
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
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

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: RetentionViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
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
            setBackButtonListener {

                if (isSearchCurrentlyShown) {
                    hideSoftKeyboard()
                } else {
                    findNavController().navigateUp()
                }
            }

            changeBackButtonDrawable()
            lifecycleScope.launchWhenCreated {

                search_item.getTextChangeAsStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->

                        Log.d("Search ", "Searhcingg...$searchString")
//                        viewModel.setEvent(
//                            UpcomingGigersViewContract.UpcomingGigersUiEvents.FilterApplied.SearchFilterApplied(
//                                searchString
//                            )
//                        )
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
            viewModel.setEvent(RetentionFragmentViewContract.RetentionFragmentViewEvents.RefreshRetentionDataClicked)
        }
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is RetentionFragmentViewContract.RetentionFragmentViewUiEffects.DialogPhoneNumber -> dialPhoneNumber(
                        it.phoneNumber
                    )
                    is RetentionFragmentViewContract.RetentionFragmentViewUiEffects.OpenGigerDetailsBottomSheet -> openGigerDetailsScreen(
                        it.gigerDetails
                    )
                    is RetentionFragmentViewContract.RetentionFragmentViewUiEffects.ShowDateFilterBottomSheet -> showDateFilter(
                        it.filters
                    )
                    is RetentionFragmentViewContract.RetentionFragmentViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                }
            }
    }

    private fun showDateFilter(filters: List<TLWorkSpaceFilterOption>) {

    }

    private fun openGigerDetailsScreen(
        gigerDetails: RetentionScreenData.GigerItemData
    ) {

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
                    is RetentionFragmentViewContract.RetentionFragmentUiState.ErrorWhileLoadingRetentionData -> handleErrorInLoadingData(
                        it.error
                    )
                    is RetentionFragmentViewContract.RetentionFragmentUiState.LoadingRetentionData -> handleLoadingState(
                        it.alreadyShowingGigersOnView
                    )
                    RetentionFragmentViewContract.RetentionFragmentUiState.ScreenInitialisedOrRestored -> {}
                    is RetentionFragmentViewContract.RetentionFragmentUiState.ShowOrUpdateRetentionData -> handleDataLoadedState(
                        it.dateFilterSelected,
                        it.retentionData
                    )
                }
            }
    }

    private fun handleErrorInLoadingData(
        error: String
    ) = viewBinding.apply {

    }

    private fun handleDataLoadedState(
        gigers: TLWorkSpaceFilterOption,
        retentionData: List<RetentionScreenData>
    ) = viewBinding.apply {

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

//        infoLayout.gone()
        this.retentionMainLayout.recyclerView.collection = retentionData
        showOrHideNoDataLayout(
            retentionData.isNotEmpty()
        )
    }

    private fun showOrHideNoDataLayout(
        dataAvailableToShowOnScreen: Boolean
    ) = viewBinding.apply {

        if (dataAvailableToShowOnScreen) {
//            infoLayout.root.visible()
//            infoLayout.infoMessageTv.text = "Nothing to show yet, please check later"
        } else {
//            infoLayout.root.gone()
//            infoLayout.infoMessageTv.text = null
        }
    }

    private fun handleLoadingState(
        anyPreviousDataShownOnScreen: Boolean
    ) = viewBinding.apply {

        if (anyPreviousDataShownOnScreen) {

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