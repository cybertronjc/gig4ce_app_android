package com.gigforce.app.tl_work_space.upcoming_gigers

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentUpcomingGigersBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.upcoming_gigers.models.UpcomingGigersListData
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
                        viewModel.setEvent(
                            UpcomingGigersViewContract.UpcomingGigersUiEvents.FilterApplied.SearchFilterApplied(
                                searchString
                            )
                        )
                    }
            }
        }


        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(UpcomingGigersAdapterDiffUtil())
        recyclerView.setHasFixedSize(true)

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
                    is UpcomingGigersViewContract.UpcomingGigersViewUiEffects.DialogPhoneNumber -> dialPhoneNumber(
                        it.phoneNumber
                    )
                    is UpcomingGigersViewContract.UpcomingGigersViewUiEffects.OpenGigerDetailsBottomSheet -> openGigerDetailsScreen(
                        it.gigerDetails
                    )
                }
            }
    }

    private fun openGigerDetailsScreen(
        gigerDetails: UpcomingGigersListData.UpcomingGigerItemData
    ) {

    }

    private fun dialPhoneNumber(
        phoneNumber: String
    ) {

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
                        it.upcomingGigers
                    )
                }
            }
    }

    private fun handleErrorInLoadingData(
        error: String
    ) = viewBinding.apply {

    }

    private fun handleDataLoadedState(
        gigers: List<UpcomingGigersListData>
    ) = viewBinding.apply {

        swipeRefreshLayout.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

//        infoLayout.gone()
        recyclerView.collection = gigers
        showOrHideNoDataLayout(
            gigers.isNotEmpty()
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