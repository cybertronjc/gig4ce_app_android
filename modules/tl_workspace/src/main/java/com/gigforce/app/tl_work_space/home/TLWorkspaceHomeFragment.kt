package com.gigforce.app.tl_work_space.home

import android.animation.Animator
import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.OvershootInterpolator
import android.widget.DatePicker
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.domain.models.tl_workspace.TLWorkSpaceDateFilterOption
import com.gigforce.app.domain.models.tl_workspace.TLWorkspaceHomeSection
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.navigation.tl_workspace.attendance.ActivityTrackerNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.databinding.FragmentTlWorkspaceHomeBinding
import com.gigforce.app.tl_work_space.home.models.TLWorkspaceRecyclerItemData
import com.gigforce.app.tl_work_space.home.views.ActionAttachmentOptionsListetner
import com.gigforce.app.tl_work_space.home.views.ActionsAttachmentOption
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.google.android.material.snackbar.Snackbar
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.OnMenuItemClickListener
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class TLWorkspaceHomeFragment : BaseFragment2<FragmentTlWorkspaceHomeBinding>(
    fragmentName = "TLWorkspaceHomeFragment",
    layoutId = R.layout.fragment_tl_workspace_home,
    statusBarColor = R.color.status_bar_pink
), OnMenuItemClickListener<PowerMenuItem>, ActionAttachmentOptionsListetner {

    companion object {
        const val TAG = "TLWorkspaceHomeFragment"
    }

    @Inject
    lateinit var tlWorkSpaceNavigation: TLWorkSpaceNavigation

    @Inject
    lateinit var activityTrackerNavigation: ActivityTrackerNavigation

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

    private fun initView() = viewBinding.apply {
        this.appBar.apply {
            changeBackButtonDrawable()
            setBackButtonListener {
                findNavController().navigateUp()
            }

            changeBackButtonDrawable()
            makeBackgroundMoreRound()
            makeTitleBold()
            makeHelpVisible(true)

            helpImageButton.setOnClickListener {
                tlWorkSpaceNavigation.navigateToHelpScreen()
            }
        }

        actionsAttachment.setAttachmentOptions(
            ActionsAttachmentOption.allOptionsList,
            ActionsAttachmentOption.quickOptionsList,
            this@TLWorkspaceHomeFragment
        )

        middleView.setOnClickListener {
            if (middleView.isVisible && actionsAttachment.isAttachmentOptionViewVisible()){
                actionsAttachment.hideAttachmentOptionView()
                middleView.gone()
            } else {
                //do nothing
            }
        }


        rootLayout.setOnTouchListener { view, motionEvent ->
            actionsAttachment.hideAttachmentOptionView()
            view.foreground = null
            true
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(TLWorkSpaceHomeAdapterDiffUtil())
        recyclerView.setHasFixedSize(true)
        recyclerView.isNestedScrollingEnabled = true

        swipeRefreshLayout.setOnRefreshListener {
            viewModel.setEvent(TLWorkSpaceHomeUiEvents.RefreshWorkSpaceDataClicked)
        }

        infoLayout.infoIv.loadImage(R.drawable.ic_dragon_sleeping_animation)
    }

    private fun observeViewEffects() = lifecycleScope.launchWhenCreated {

        viewModel
            .effect
            .collect {

                when (it) {
                    is TLWorkSpaceHomeViewUiEffects.NavigationEvents -> handleNavigationEvent(
                        it
                    )
                    is TLWorkSpaceHomeViewUiEffects.ShowFilterDialog -> showFilterMenu(
                        it.anchorView,
                        it.dateFilters,
                        it.sectionId
                    )
                    is TLWorkSpaceHomeViewUiEffects.ShowSnackBar -> showSnackBar(
                        it.message
                    )
                    is TLWorkSpaceHomeViewUiEffects.OpenDateSelectDialog -> openDateFilter(
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

    private fun handleNavigationEvent(
        it: TLWorkSpaceHomeViewUiEffects.NavigationEvents
    ) {
        when (it) {
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenCompliancePendingScreen -> tlWorkSpaceNavigation.navigateToPendingComplianceScreen(
                it.title
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenUpcomingGigersScreen -> tlWorkSpaceNavigation.navigateToUpcomingGigersScreen(
                it.title
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenActivityTrackerScreen -> tlWorkSpaceNavigation.navigateToActivityTrackerListScreen(
                it.title
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenGigerDetailsBottomSheet -> tlWorkSpaceNavigation.openGigerInfoBottomSheet(
                it.gigerId
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenJoininingScreen -> tlWorkSpaceNavigation.navigateToJoiningListScreen(
                it.title
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenPayoutScreen -> tlWorkSpaceNavigation.navigateToPayoutListScreen(
                it.title
            )
            is TLWorkSpaceHomeViewUiEffects.NavigationEvents.OpenRetentionScreen -> tlWorkSpaceNavigation.navigateToRetentionScreen(
                it.title
            )
        }
    }


    private fun openDateFilter(
        sectionId: String,
        filterId: String,
        showRange: Boolean,
        selectedDate: LocalDate,
        minDate: LocalDate?,
        maxDate: LocalDate?
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
        minDate: LocalDate?,
        maxDate: LocalDate?
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
                    TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
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
        minDate: LocalDate?,
        maxDate: LocalDate?
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
                    TLWorkSpaceHomeUiEvents.DateSelectedInCustomDateFilter(
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
        dateFilters: List<TLWorkSpaceDateFilterOption>,
        sectionId: String
    ) {
        val anyValueSelected = dateFilters.find {
            it.selected
        } != null

        PowerMenu.Builder(requireContext()).apply {

            dateFilters.forEach {
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
        }.setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT).
            .setMenuRadius(10f) // sets the corner radius.
            .setMenuShadow(10f) // sets the shadow.
            .setTextColor(ContextCompat.getColor(requireContext(), R.color.inkDefault))
            .setTextGravity(Gravity.START)
            .setSelectedTextColor(ContextCompat.getColor(requireContext(), R.color.inkDefault))
            .setMenuColor(Color.WHITE)
            .setSelectedMenuColor(ContextCompat.getColor(requireContext(), R.color.blue_50))
            .setOnMenuItemClickListener(this@TLWorkspaceHomeFragment)
            .setAutoDismiss(true)
            .setShowBackground(false)
            .setLifecycleOwner(viewLifecycleOwner)
            .build()
            .showAsAnchorLeftBottom(anchorView)
    }


    private fun observeViewStates() = lifecycleScope.launchWhenCreated {

        viewModel.uiState
            .collect {

                logger.d(TAG, "State : $it")
                when (it) {
                    is TLWorkSpaceHomeUiState.ErrorWhileLoadingScreenContent -> handleErrorInLoadingData(
                        it.error
                    )
                    is TLWorkSpaceHomeUiState.LoadingHomeScreenContent -> handleLoadingState(
                        it.anyPreviousDataShownOnScreen
                    )
                    is TLWorkSpaceHomeUiState.ShowOrUpdateSectionListOnView -> handleDataLoadedState(
                        it.sectionData
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

        if (recyclerView.collection.isEmpty()) {
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = error
        } else {
            infoLayout.root.gone()
            showSnackBar(error)
        }
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

        infoLayout.root.gone()
        recyclerView.collection = sectionData
        showOrHideNoDataLayout(
            sectionData.isNotEmpty()
        )
    }

    private fun showOrHideNoDataLayout(
        dataAvailableToShowOnScreen: Boolean
    ) = viewBinding.apply {

        if (dataAvailableToShowOnScreen) {
            infoLayout.root.gone()
            infoLayout.infoMessageTv.text = null
        } else {
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = "Nothing to show yet, please check later"
        }
    }

    private fun handleLoadingState(
        anyPreviousDataShownOnScreen: Boolean
    ) = viewBinding.apply {
        logger.v(TAG, "showing loading state : previousDataShown : $anyPreviousDataShownOnScreen")
        infoLayout.root.gone()

        if (anyPreviousDataShownOnScreen) {

            if (!swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = true
            }

            shimmerContainer.gone()
            stopShimmer(
                this.shimmerContainer,
                R.id.shimmer_controller
            )
        } else {

            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }

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

    override fun onItemClick(position: Int, item: PowerMenuItem?): Unit = item.let {
        val filterTag = it?.tag?.toString() ?: return@let


        val sectionId = filterTag.substringBefore("<>")
        val filterId = filterTag.substringAfter("<>")

        viewModel.setEvent(
            TLWorkSpaceHomeUiEvents.FilterSelected(
                section = TLWorkspaceHomeSection.fromId(sectionId),
                filterId = filterId
            )
        )
    }

    override fun onClick(attachmentOption: ActionsAttachmentOption?) {
        when(attachmentOption?.id) {
            ActionsAttachmentOption.SELECTION_FORM_ID -> {
                tlWorkSpaceNavigation.navigateToJoiningListScreen(
                    "Selection Form"
                )
            }
            ActionsAttachmentOption.LOGIN_SUMMARY_ID -> {


            }
            ActionsAttachmentOption.RAISE_GIGER_TICKET_ID -> {

            }
            ActionsAttachmentOption.GIGER_ATTENDANCE_ID -> {

            }
            ActionsAttachmentOption.ALL_SELECTIONS_ID -> {

            }
            ActionsAttachmentOption.COMPLIANCE_PENDING_ID -> {


            }
            ActionsAttachmentOption.GIGER_PAYOUT_ID -> {
                tlWorkSpaceNavigation.navigateToPayoutListScreen(
                    "Payout"
                )
            }
            ActionsAttachmentOption.GIGER_RETENTION_ID -> {

            }
            ActionsAttachmentOption.GIGER_TICKET_ID -> {

            }

        }
    }

    override fun isVisible(visible: Boolean?) {
        if (visible == true){
            viewBinding.middleView.visible()
        } else {
            viewBinding.middleView.gone()
        }
    }
}