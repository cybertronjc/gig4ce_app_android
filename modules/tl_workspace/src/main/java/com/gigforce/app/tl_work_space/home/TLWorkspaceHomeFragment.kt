package com.gigforce.app.tl_work_space.home

import android.app.DatePickerDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.android_common_utils.base.viewModel.UiEvent
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentTlWorkspaceHomeBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.google.android.material.snackbar.Snackbar
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate

@AndroidEntryPoint
class TLWorkspaceHomeFragment : BaseFragment2<FragmentTlWorkspaceHomeBinding>(
    fragmentName = "TLWorkspaceHomeFragment",
    layoutId = R.layout.fragment_tl_workspace_home,
    statusBarColor = R.color.status_bar_pink
), OnMenuItemClickListener<PowerMenuItem> {
    private val viewModel: TLWorkspaceHomeViewModel by viewModels()

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

    private fun initView()  = viewBinding.apply{

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(TLWorkSpaceHomeAdapterDiffUtil())
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.RefreshWorkSpaceDataClicked)
        }
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.ShowFilterDialog -> showFilterMenu(
                        it.anchorView,
                        it.filters,
                        it.sectionId
                    )
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeViewUiEffects.OpenDateSelectedDialog -> openDateFilter(
                        it.sectionId,
                        it.filterId,
                        it.showRange,
                        it.selectedDate,
                        it.minDate,
                        it.maxDate
                    )
                }
            }
    }


    private fun openDateFilter(
        sectionId: String,
        filterId: String,
        showRange: Boolean,
        selectedDate: LocalDate,
        minDate: LocalDate,
        maxDate: LocalDate
    ) {
        if (showRange) {

            openSelectDateRangeSelectionDialog(
                sectionId,
                filterId,
                selectedDate,
                minDate,
                maxDate
            )
        } else {
            openSingleDateSelectionDialog(
                sectionId,
                filterId,
                selectedDate,
                minDate,
                maxDate
            )
        }
    }

    private fun openSingleDateSelectionDialog(
        sectionId: String,
        filterId: String,
        defaultDate: LocalDate,
        minDate: LocalDate,
        maxDate: LocalDate
    ) {
        DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { datePickerView: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val filterTag = datePickerView?.tag?.toString() ?: return@DatePickerDialog

                val currentViewSectionId = filterTag.substringBefore("<>")
                val currentViewFilterId = filterTag.substringAfter("<>")

                val date = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )
                viewModel.setEvent(
                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
                        currentViewSectionId,
                        currentViewFilterId,
                        date,
                        null
                    )
                )
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        ).apply {
            val menuTag = "$sectionId<>$filterId"
            this.datePicker.tag = menuTag

            this.show()
        }
    }

    private fun openSelectDateRangeSelectionDialog(
        sectionId: String,
        filterId: String,
        defaultDate: LocalDate,
        minDate: LocalDate,
        maxDate: LocalDate
    ) {
        DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { datePickerView: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val filterTag = datePickerView?.tag?.toString() ?: return@DatePickerDialog

                val currentViewSectionId = filterTag.substringBefore("<>")
                val currentViewFilterId = filterTag.substringAfter("<>")

                val date = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )
                viewModel.setEvent(
                    TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
                        currentViewSectionId,
                        currentViewFilterId,
                        date,
                        null
                    )
                )
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        ).apply {
            val menuTag = "$sectionId<>$filterId"
            this.datePicker.tag = menuTag

            this.show()
        }
    }


    private fun showFilterMenu(
        anchorView: View,
        filters: List<TLWorkSpaceFilterOption>,
        sectionId: String
    ) {
        val anyValueSelected = filters.find {
            it.selected
        } != null

        PowerMenu.Builder(requireContext()).apply {

            filters.forEach {
                val menuTag = sectionId + "<>" + it.filterId
                if (anyValueSelected) {

                    addItem(
                        PowerMenuItem(
                            it.text,
                            it.selected,
                            menuTag
                        )
                    )
                } else {

                    addItem(
                        PowerMenuItem(
                            it.text,
                            it.default,
                            menuTag
                        )
                    )
                }
            }
        }.setAnimation(MenuAnimation.SHOWUP_TOP_LEFT) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.inkDefault))
            .setTextGravity(Gravity.START)
            .setTextTypeface(Typeface.create("sans-serif-medium", Typeface.BOLD))
            .setSelectedTextColor(ContextCompat.getColor(requireContext(), R.color.inkDefault))
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(requireContext(), R.color.blue_50))
            .setOnMenuItemClickListener(this@TLWorkspaceHomeFragment)
            .build()
            .showAsAnchorCenter(anchorView)
    }


    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                when (it) {
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent -> handleErrorInLoadingData(
                        it.error
                    )
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.LoadingHomeScreenContent -> handleLoadingState(
                        it.anyPreviousDataShownOnScreen
                    )
                    is TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiState.ShowOrUpdateSectionListOnView -> handleDataLoadedState(
                        it.sectionData
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
    ) = viewBinding.apply{

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

        val sectionId = filterTag.substringBefore("<>")
        val filterId = filterTag.substringAfter("<>")

        viewModel.setEvent(
            TLWorkSpaceHomeViewContract.TLWorkSpaceHomeUiEvents.FilterSelected(
                section = TLWorkspaceHomeSection.fromId(sectionId),
                filterId = filterId
            )
        )
    }
}