package com.gigforce.app.tl_work_space.upcoming_gigers

import android.os.Bundle
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentTlWorkspaceHomeBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.google.android.material.snackbar.Snackbar
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenuItem
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UpcomingGigersFragment : BaseFragment2<FragmentTlWorkspaceHomeBinding>(
    fragmentName = "UpcomingGigersFragment",
    layoutId = R.layout.fragment_tl_workspace_home,
    statusBarColor = R.color.status_bar_pink
), OnMenuItemClickListener<PowerMenuItem> {

    private val viewModel: UpcomingGigersViewModel by viewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentTlWorkspaceHomeBinding,
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
                    this.shimmerContainer,
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

    override fun onItemClick(position: Int, item: PowerMenuItem?): Unit = item.let {
        val filterTag = it?.tag?.toString() ?: return@let
        val filterId = filterTag.substringAfter("<>")

//        viewModel.setEvent(
//            UpcomingGigersViewContract.UpcomingGigersUiEvents.FilterApplied.DateFilterApplied(
//                filterId = filterId
//            )
//        )
    }

//    private fun openDateFilter(
//        sectionId: String,
//        filterId: String,
//        showRange: Boolean,
//        selectedDate: LocalDate,
//        minDate: LocalDate,
//        maxDate: LocalDate
//    ) {
//        if (showRange) {
//
//            openSelectDateRangeSelectionDialog(
//                sectionId,
//                filterId,
//                selectedDate,
//                minDate,
//                maxDate
//            )
//        } else {
//            openSingleDateSelectionDialog(
//                sectionId,
//                filterId,
//                selectedDate,
//                minDate,
//                maxDate
//            )
//        }
//    }

//    private fun openSingleDateSelectionDialog(
//        sectionId: String,
//        filterId: String,
//        defaultDate: LocalDate,
//        minDate: LocalDate,
//        maxDate: LocalDate
//    ) {
//        DatePickerDialog(
//            requireContext(),
//            R.style.DatePickerTheme,
//            { datePickerView: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
//                val filterId = datePickerView?.tag?.toString() ?: return@DatePickerDialog
//
//                val date = LocalDate.of(
//                    year,
//                    month + 1,
//                    dayOfMonth
//                )
//                viewModel.setEvent(
//                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
//                        currentViewSectionId,
//                        currentViewFilterId,
//                        date,
//                        null
//                    )
//                )
//            },
//            defaultDate.year,
//            defaultDate.monthValue - 1,
//            defaultDate.dayOfMonth
//        ).apply {
//            val menuTag = "$sectionId<>$filterId"
//            this.datePicker.tag = menuTag
//
//            this.show()
//        }
//    }
//
//    private fun openSelectDateRangeSelectionDialog(
//        sectionId: String,
//        filterId: String,
//        defaultDate: LocalDate,
//        minDate: LocalDate,
//        maxDate: LocalDate
//    ) {
//        DatePickerDialog(
//            requireContext(),
//            R.style.DatePickerTheme,
//            { datePickerView: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
//                val filterTag = datePickerView?.tag?.toString() ?: return@DatePickerDialog
//
//                val currentViewSectionId = filterTag.substringBefore("<>")
//                val currentViewFilterId = filterTag.substringAfter("<>")
//
//                val date = LocalDate.of(
//                    year,
//                    month + 1,
//                    dayOfMonth
//                )
////                viewModel.setEvent(
////                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
////                        currentViewSectionId,
////                        currentViewFilterId,
////                        date,
////                        null
////                    )
////                )
//            },
//            defaultDate.year,
//            defaultDate.monthValue - 1,
//            defaultDate.dayOfMonth
//        ).apply {
//            val menuTag = "$sectionId<>$filterId"
//            this.datePicker.tag = menuTag
//
//            this.show()
//        }
//    }
}