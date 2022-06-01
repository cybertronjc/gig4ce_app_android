package com.gigforce.app.tl_work_space.payout

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
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

@AndroidEntryPoint
class GigerPayoutFragment : BaseFragment2<GigerPayoutFragmentBinding>(
    fragmentName = "GigerPayoutFragment",
    layoutId = R.layout.giger_payout_fragment,
    statusBarColor = R.color.status_bar_pink
) {

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation
    private val viewModel: GigerPayoutViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
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
        dateFilterSelected: TLWorkSpaceFilterOption?,
        gigerPayoutData: List<GigerPayoutScreenData>,
        updatedTabMaster: List<GigerPayoutStatusData>
    ) = viewBinding.apply{
        Log.d("PayoutData", "gigers: $gigerPayoutData")
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

    private fun showOrHideNoDataLayout(dataAvailableToShowOnScreen: Boolean) = viewBinding.apply{
        if (dataAvailableToShowOnScreen) {
//            infoLayout.root.visible()
//            infoLayout.infoMessageTv.text = "Nothing to show yet, please check later"
        } else {
//            infoLayout.root.gone()
//            infoLayout.infoMessageTv.text = null
        }

    }

    private fun handleLoadingState(alreadyShowingGigersOnView: Boolean) = viewBinding.apply{
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

    private fun handleErrorLoadingInPayoutData(error: String)  = viewBinding.apply{


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

    private fun showDateFilterBottomSheet(filters: List<TLWorkSpaceFilterOption>) {

    }

    private fun openGigerDetailScreen(gigerDetails: GigerPayoutScreenData.GigerItemData) {


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