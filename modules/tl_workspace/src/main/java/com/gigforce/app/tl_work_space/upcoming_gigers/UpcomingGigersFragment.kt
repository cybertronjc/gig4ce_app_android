package com.gigforce.app.tl_work_space.upcoming_gigers

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation.Companion.FRAGMENT_RESULT_KEY_DATE_FILTER
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentTlWorkspaceHomeBinding
import com.gigforce.app.tl_work_space.databinding.FragmentUpcomingGigersBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UpcomingGigersFragment : BaseFragment2<FragmentUpcomingGigersBinding>(
    fragmentName = "UpcomingGigersFragment",
    layoutId = R.layout.fragment_upcoming_gigers,
    statusBarColor = R.color.status_bar_pink
) {

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: UpcomingGigersViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentUpcomingGigersBinding,
        savedInstanceState: Bundle?
    ) {
        if (viewCreatedForTheFirstTime) {
            initView()
            observeViewStates()
            observeViewEffects()
        }
    }

    private fun initView() = viewBinding.apply {

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
//        recyclerView.setDiffUtilCallback(TLWorkSpaceHomeAdapterDiffUtil())
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(UpcomingGigersViewContract.UpcomingGigersUiEvents.RefreshUpcomingGigersClicked)
        }
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is UpcomingGigersViewContract.UpcomingGigersViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                    is UpcomingGigersViewContract.UpcomingGigersViewUiEffects.ShowFilterBottomSheet -> tlWorkSpaceNavigation.openFilterBottomSheet(
                        it.filters
                    )
                }
            }
    }


    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                when (it) {
                    is UpcomingGigersViewContract.UpcomingGigersUiState.ErrorWhileLoadingScreenContent -> handleErrorInLoadingData(
                        it.error
                    )
                    is UpcomingGigersViewContract.UpcomingGigersUiState.LoadingGigers -> handleLoadingState(
                        it.alreadyShowingGigersOnView
                    )
                    is UpcomingGigersViewContract.UpcomingGigersUiState.ShowOrUpdateSectionListOnView -> handleDataLoadedState(
                        emptyList()
                    )
                }
            }
    }

    private fun handleErrorInLoadingData(
        error: String
    ) = viewBinding.apply {

    }

    private fun handleDataLoadedState(
        sectionData: List<TLWorkspaceRecyclerItemData>
    ) = viewBinding.apply {

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

//        infoLayout.gone()
        recyclerView.collection = sectionData
        showOrHideNoDataLayout(
            sectionData.isNotEmpty()
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