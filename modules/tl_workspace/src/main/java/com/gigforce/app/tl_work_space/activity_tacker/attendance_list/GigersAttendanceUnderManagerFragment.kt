package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.android_common_utils.extensions.capitalizeFirstLetter
import com.gigforce.app.data.repositoriesImpl.gigs.models.GigAttendanceData
import com.gigforce.app.navigation.gigs.GigNavigation
import com.gigforce.app.navigation.tl_workspace.attendance.ActivityTrackerNavigation
import com.gigforce.app.tl_work_space.R
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceStatusAndCountItemData
import com.gigforce.app.tl_work_space.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.common_ui.DisplayUtil.px
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.hideSoftKeyboard
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class GigersAttendanceUnderManagerFragment :
    BaseFragment2<FragmentGigerUnderManagersAttendanceBinding>(
        fragmentName = TAG,
        layoutId = R.layout.fragment_giger_under_managers_attendance,
        statusBarColor = R.color.lipstick_2
    ), AttendanceSwipeHandler.AttendanceSwipeHandlerListener,
    IOnBackPressedOverride {

    companion object {
        const val TAG = "GigerAttendanceUnderManagerFrg"
    }

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var gigNavigation: GigNavigation

    @Inject
    lateinit var tlWorkspaceActivityTrackerNavigation: ActivityTrackerNavigation

    private val sharedGigViewModel: AttendanceTLSharedViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private val gigJoiningSharedViewModel: SharedGigViewModel by activityViewModels()
    private val simpleDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    private val swipeTouchHandler = AttendanceSwipeHandler(this)
    private val itemTouchHelper = ItemTouchHelper(swipeTouchHandler)

    private val datePicker: DatePickerDialog by lazy {

        val defaultDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            R.style.DatePickerTheme,
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                val date = LocalDate.of(
                    year,
                    month + 1,
                    dayOfMonth
                )

                viewModel.fetchUsersAttendanceDate(date)
            },
            defaultDate.year,
            defaultDate.monthValue - 1,
            defaultDate.dayOfMonth
        )

        datePickerDialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData(savedInstanceState)
    }

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun viewCreated(
        viewBinding: FragmentGigerUnderManagersAttendanceBinding,
        savedInstanceState: Bundle?
    ) {

        if (viewCreatedForTheFirstTime) {
            initView()
            initViewModel()
        }
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    @OptIn(FlowPreview::class)
    private fun initView() = viewBinding.apply {

        viewBinding.appBarComp.setBackButtonListener {
            if (viewBinding.appBarComp.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else {
                activity?.onBackPressed()
            }
        }

        lifecycleScope.launch {

            viewBinding.appBarComp.apply {

                if (title.isNotBlank())
                    titleText.text = title
                else
                    titleText.text = "Gigers Attendance"

                changeBackButtonDrawable()
                filterImageButton.setImageResource(R.drawable.ic_calendar_mono_white)
                filterImageButton.setOnClickListener {
                    datePicker.show()
                }

                search_item.getTextChangeAsStateFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->
                        Log.d("Search ", "Searhcingg...$searchString")
                        viewModel.searchAttendance(searchString)
                    }
            }
        }

        setDefaultTabs()
        infoLayout.infoIv.loadImage(R.drawable.ic_dragon_sleeping_animation)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(TLAttendanceAdapterDiffUtil())
        itemTouchHelper.attachToRecyclerView(recyclerView)

        swipeRefresh.setOnRefreshListener {
            viewModel.handleEvent(
                GigerAttendanceUnderManagerViewContract.UiEvent.RefreshAttendanceClicked
            )
        }

        swipeDirectionTextSwitcher.setFactory {
            val textView = TextView(requireContext())
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.0f)
            textView.setTextColor(ResourcesCompat.getColor(resources, android.R.color.black, null))

            val font = ResourcesCompat.getFont(requireContext(), R.font.lato)
            textView.typeface = font

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                textView.lineHeight = 12
            }
            textView
        }
        swipeDirectionTextSwitcher.setInAnimation(requireContext(), R.anim.animscale_in)
        swipeDirectionTextSwitcher.setOutAnimation(requireContext(), R.anim.animscale_out)
    }

    private fun setDefaultTabs() = viewBinding.tablayout.apply {

        addTab(
            newTab().apply {
                text = "Enabled (0)"
                tag = StatusFilters.ENABLED
            }
        )
        addTab(
            newTab().apply {
                text = "Inactive (0)"
                tag = StatusFilters.INACTIVE
            }
        )
        addTab(
            newTab().apply {
                text = "Active (0)"
                tag = StatusFilters.ACTIVE
            }
        )

        val tabs = getChildAt(0) as ViewGroup
        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0f

            layoutParams.marginEnd = 12.px
            tab.layoutParams = layoutParams
            requestLayout()
        }

        onTabSelected {
            val tab = it ?: return@onTabSelected
            viewModel.filterAttendanceByStatus(
                tab.tag.toString()
            )
        }
    }

    private fun initViewModel() {
        viewModel.setGigsSharedViewModel(sharedGigViewModel)
        viewModel.setGigsJoiningSharedViewModel(gigJoiningSharedViewModel)

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

                    logger.d(TAG, "state recevied : $it")
                    when (it) {
                        is GigerAttendanceUnderManagerViewContract.State.ErrorInLoadingOrUpdatingAttendanceList -> errorInLoadingAttendanceFromServer(
                            it.error,
                            false
                        )
                        is GigerAttendanceUnderManagerViewContract.State.LoadingAttendanceList -> showDataLoadingFromServer()
                        GigerAttendanceUnderManagerViewContract.State.ScreenLoaded -> {}
                        is GigerAttendanceUnderManagerViewContract.State.ShowOrUpdateAttendanceListOnView -> {
                            showStatusAndAttendanceOnView(
                                attendanceSwipeControlsEnabled = it.attendanceSwipeControlsEnabled,
                                enablePresentSwipeAction = it.enablePresentSwipeAction,
                                enableDeclineSwipeAction = it.enableDeclineSwipeAction,
                                showDataUpdateSnackbar = it.showUpdateToast,
                                attendanceItemData = it.attendanceItemData
                            )

                            if (it.tabsDataCounts != null) {
                                setStatusTabs(
                                    it.tabsDataCounts
                                )
                            }

                            val showingDataForDateFormatted = simpleDateFormat.format(it.date)
                            if (showingDataForDateFormatted != viewBinding.appBarComp.getSubTitleText()) {
                                //If date is same then user might have just expanded/collapsed some layout

                                viewBinding.appBarComp.showSubtitle(
                                    showingDataForDateFormatted
                                )
                                if (it.attendanceSwipeControlsEnabled) {
                                    viewBinding.swipeDirectionLabelLayout.isVisible = true

                                    viewBinding.swipeDirectionTextSwitcher.setText(
                                        if (it.enablePresentSwipeAction && it.enableDeclineSwipeAction) {
                                            "Swipe right to mark present and left to mark absent."
                                        } else {
                                            "Swipe left to mark absent."
                                        }
                                    )
                                } else {
                                    viewBinding.swipeDirectionLabelLayout.isVisible = false
                                }
                            }
                        }
                    }
                }
        }

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewEffects
                .collect {

                    when (it) {
                        is GigerAttendanceUnderManagerViewContract.UiEffect.ShowErrorUnableToMarkAttendanceForUser -> showErrorInMarkingAttendance(
                            it.error
                        )
                        is GigerAttendanceUnderManagerViewContract.UiEffect.ShowGigerDetailsScreen -> showGigerDetailsScreen(
                            it.gigId,
                            it.gigAttendanceData
                        )
                        is GigerAttendanceUnderManagerViewContract.UiEffect.ShowResolveAttendanceConflictScreen -> showResolveAttendanceConflictScreen(
                            it.gigId,
                            it.gigAttendanceData
                        )
                        is GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkGigerActiveConfirmation -> openMarkActiveConfirmationDialog(
                            it.gigId,
                            it.hasGigerMarkedHimselfInActive
                        )
                        is GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkInactiveConfirmationDialog -> openMarkInactiveConfirmationDialog(
                            gigId = it.gigId
                        )
                        is GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkInactiveSelectReasonDialog -> openMarkInactiveSelectReasonDialog(
                            gigId = it.gigId,
                            popConfirmationDialog = it.popConfirmationDialog
                        )
                    }
                }
        }
    }


    private fun openMarkInactiveConfirmationDialog(
        gigId: String
    ) {
        tlWorkspaceActivityTrackerNavigation.openMarkInactiveConfirmationDialog(
            gigId = gigId,
            hasGigerMarkedHimselfActive = false
        )
    }

    private fun openMarkInactiveSelectReasonDialog(
        popConfirmationDialog: Boolean,
        gigId: String
    ) {
        if (popConfirmationDialog) {
            navigation.navigateUp()
        }

        tlWorkspaceActivityTrackerNavigation.openMarkInactiveReasonDialog(
            gigId = gigId
        )
    }

    private fun openMarkActiveConfirmationDialog(
        gigId: String,
        hasGigerMarkedHimselfInActive: Boolean
    ) {
        tlWorkspaceActivityTrackerNavigation.openActiveConfirmationDialog(
            gigId = gigId,
            hasGigerMarkedHimselfInactive = hasGigerMarkedHimselfInActive
        )
    }

    private fun showResolveAttendanceConflictScreen(
        gigId: String,
        gigAttendanceData: GigAttendanceData
    ) {
        tlWorkspaceActivityTrackerNavigation.openResolveAttendanceConflictDialog(
            gigId = gigId,
            attendanceDetails = gigAttendanceData
        )
    }

    private fun showGigerDetailsScreen(
        gigId: String,
        gigAttendanceData: GigAttendanceData
    ) {
        tlWorkspaceActivityTrackerNavigation.openGigDetailsScreen(
            gigId = gigId,
            attendanceDetails = gigAttendanceData
        )
    }

    private fun showErrorInMarkingAttendance(
        error: String
    ) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Unable to mark present")
            .setMessage(error)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun showStatusAndAttendanceOnView(
        attendanceSwipeControlsEnabled: Boolean,
        enablePresentSwipeAction: Boolean,
        enableDeclineSwipeAction: Boolean,
        showDataUpdateSnackbar: Boolean,
        attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        swipeTouchHandler.attendanceSwipeControlsEnabled = attendanceSwipeControlsEnabled
        swipeTouchHandler.markPresentSwipeActionEnabled = enablePresentSwipeAction
        swipeTouchHandler.declineSwipeActionEnabled = enableDeclineSwipeAction

        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()
        infoLayout.root.gone()
        recyclerView.collection = attendanceItemData
        swipeRefresh.isRefreshing = false

        val itemsShown = recyclerView.adapter?.itemCount ?: 0
        if (itemsShown != 0 && showDataUpdateSnackbar) {
            Snackbar.make(viewBinding.rootFrameLayout, "Attendance Updated", Snackbar.LENGTH_SHORT)
                .show()
        }

        showOrHideNoAttendanceLayout(attendanceItemData.size)
    }

    private fun showOrHideNoAttendanceLayout(
        payoutsCount: Int
    ) = viewBinding.apply {

        if (payoutsCount == 0) {
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = "No giger to show"
        } else {
            infoLayout.root.gone()
            infoLayout.infoMessageTv.text = null
        }
    }

    private fun setStatusTabs(
        attendanceStatuses: List<AttendanceStatusAndCountItemData>
    ) = viewBinding.apply {

        attendanceStatuses.forEach {

            for (i in 0 until tablayout.tabCount) {

                val tab = tablayout.getTabAt(i)
                val tabStatus = tab!!.tag.toString()
                val tabText = tabStatus.capitalizeFirstLetter()

                if (tabStatus == it.status) {
                    tab.text = "$tabText (${it.attendanceCount})"
                    break
                }
            }
        }
    }


    private fun errorInLoadingAttendanceFromServer(
        error: String,
        shouldShowRetryButton: Boolean
    ) = viewBinding.apply {
        swipeRefresh.isRefreshing = false
        stopShimmer(
            shimmerContainer,
            R.id.shimmer_controller
        )
        shimmerContainer.gone()

        if (recyclerView.childCount == 0) {

            swipeDirectionLabelLayout.isVisible = false
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = error
        } else {
            infoLayout.root.gone()

            Snackbar.make(
                rootFrameLayout,
                error,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun showDataLoadingFromServer() = viewBinding.apply {
        infoLayout.root.gone()

        if (recyclerView.childCount == 0) {

            swipeDirectionLabelLayout.isVisible = false
            shimmerContainer.visible()
            swipeRefresh.isRefreshing = false

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
        } else {

            swipeRefresh.isRefreshing = true
            shimmerContainer.gone()
            stopShimmer(
                this.shimmerContainer,
                R.id.shimmer_controller
            )
        }
    }

    override fun onRightSwipedForMarkingPresent(
        viewHolder: RecyclerView.ViewHolder,
        attendanceData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {
        viewBinding.recyclerView.coreAdapter.notifyItemChanged(
            viewHolder.adapterPosition
        )
        itemTouchHelper.startSwipe(viewHolder)

        viewModel.handleEvent(
            GigerAttendanceUnderManagerViewContract.UiEvent.UserRightSwipedForMarkingPresent(
                attendanceData
            )
        )
    }

    override fun onLeftSwipedForDecliningAttendance(
        viewHolder: RecyclerView.ViewHolder,
        attendanceData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {
        viewBinding.recyclerView.coreAdapter.notifyItemChanged(
            viewHolder.adapterPosition
        )
        itemTouchHelper.startSwipe(viewHolder)

        viewModel.handleEvent(
            GigerAttendanceUnderManagerViewContract.UiEvent.UserLeftSwipedForMarkingAbsent(
                attendanceData
            )
        )
    }

    override fun onBackPressed(): Boolean {
//        if (viewBinding.gigersUnderManagerMainLayout.slotCalendar.isVisible){
//            viewBinding.gigersUnderManagerMainLayout.slotCalendar.gone()
//            return true
//        }
        return false
    }

}