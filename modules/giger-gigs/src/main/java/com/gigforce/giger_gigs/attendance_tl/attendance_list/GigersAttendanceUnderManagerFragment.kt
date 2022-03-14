package com.gigforce.giger_gigs.attendance_tl.attendance_list

import android.os.Bundle
import android.widget.LinearLayout
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.IEventTracker
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.viewModels.SharedGigerAttendanceUnderManagerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
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
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var navigation: INavigation

    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private val simpleDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    private val swipeTouchHandler = AttendanceSwipeHandler(this)
    private val itemTouchHelper = ItemTouchHelper(swipeTouchHandler)

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

//        viewBinding.appBarComp.setBackButtonListener {
//            if (viewBinding.appBarComp.isSearchCurrentlyShown) {
//                hideSoftKeyboard()
//            } else if (viewBinding.appBarComp.slotCalendar.isVisible){
//                viewBinding.appBarComp.slotCalendar.gone()
//            } else {
//                activity?.onBackPressed()
//            }
//        }


        lifecycleScope.launch {

//            viewBinding.toolbar.apply {
//
//                if (title.isNotBlank())
//                    showTitle(title)
//                else
//                    showTitle(context.getString(R.string.gigers_attendance_giger_gigs))
//                hideActionMenu()
//                showSearchOption(context.getString(R.string.search_name_giger_gigs))
//                viewBinding.toolbar.hideSubTitle()
//                getSearchTextChangeAsFlow()
//                    .debounce(300)
//                    .distinctUntilChanged()
//                    .flowOn(Dispatchers.Default)
//                    .collect { searchString ->
//                        Log.d("Search ", "Searhcingg...$searchString")
//                        viewModel.searchAttendance(searchString)
//                    }
//            }
        }


        infoLayout.infoIv.loadImage(R.drawable.banner_no_data)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.setDiffUtilCallback(TLAttendanceAdapterDiffUtil())
        itemTouchHelper.attachToRecyclerView(recyclerView)

        swipeRefresh.setOnRefreshListener {
            viewModel.handleEvent(
                GigerAttendanceUnderManagerViewContract.UiEvent.RefreshAttendanceClicked
            )
        }
    }

    private fun initViewModel() {

        lifecycleScope.launchWhenCreated {

            viewModel
                .viewState
                .collect {

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
                        is GigerAttendanceUnderManagerViewContract.UiEffect.ShowGigerDetailsScreen -> TODO()
                        is GigerAttendanceUnderManagerViewContract.UiEffect.ShowResolveAttendanceConflictScreen -> TODO()
                    }
                }
        }
    }

    private fun showErrorInMarkingAttendance(
        error: String
    ) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.unable_to_mark_present_giger_gigs))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
            .show()
    }

    private fun showSnackBar(
        text: String
    ) {
        Snackbar.make(
            viewBinding.rootFrameLayout,
            text,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showStatusAndAttendanceOnView(
        attendanceSwipeControlsEnabled: Boolean,
        enablePresentSwipeAction: Boolean,
        enableDeclineSwipeAction: Boolean,
        showDataUpdateSnackbar : Boolean,
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
            Snackbar.make(viewBinding.rootFrameLayout,"Attendance Updated",Snackbar.LENGTH_SHORT).show()
        }

        showOrHideNoAttendanceLayout(attendanceItemData.size)
    }

    private fun showOrHideNoAttendanceLayout(
        payoutsCount: Int
    ) = viewBinding.apply {

        if (payoutsCount == 0) {
            infoLayout.root.visible()
            infoLayout.infoMessageTv.text = "No Attendance to show"
        } else {
            infoLayout.root.gone()
            infoLayout.infoMessageTv.text = null
        }
    }

    private fun setStatusTabs(
        attendanceStatuses: List<AttendanceStatusAndCountItemData>
    ) = viewBinding.apply {

//        if (shouldRemoveOlderStatusTabs) {
//            this.tablayout.removeAllTabs()
//
//            attendanceStatuses.forEach {
//
//                val newTab = this.tablayout.newTab().apply {
//                    this.text = "${it.status} (${it.attendanceCount})"
//                    this.tag = it.status
//                }
//                this.tablayout.addTab(newTab)
//            }
//        } else {
//            //Just updating Tabs Text
//            val currentShownTabsInView = mutableListOf<String>()
//            for (i in 0 until this.tablayout.tabCount) {
//
//                val tab = this.tablayout.getTabAt(i)
//                val tabStatus = tab!!.tag.toString()
//                currentShownTabsInView.add(tabStatus)
//            }
//
//            val tabsAdded = mutableListOf<AttendanceStatusAndCountItemData>()
//            val tabsDeleted = mutableListOf<String>()
//            val tabsUpdated = mutableListOf<AttendanceStatusAndCountItemData>()
//
//            attendanceStatuses.forEach {
//
//                if (!currentShownTabsInView.contains(it.status)) {
//                    tabsAdded.add(it)
//                } else {
//                    tabsUpdated.add(it)
//                }
//            }
//
//            currentShownTabsInView.forEach { tabShown ->
//
//                if (attendanceStatuses.find { it.status == tabShown } == null)
//                    tabsDeleted.add(tabShown)
//            }
//
//            tabsAdded.forEach {
//
//                val newTab = this.tablayout.newTab().apply {
//                    this.text = "${it.status} (${it.attendanceCount})"
//                    this.tag = it.status
//                }
//                this.tablayout.addTab(newTab)
//            }
//
//            for (i in 0 until tablayout.tabCount) {
//
//                val tab = tablayout.getTabAt(i)
//                val tabStatus = tab!!.tag.toString()
//
//                if (tabsDeleted.contains(tabStatus)) {
//                    tab.text = "$tabStatus (0)"
//                } else {
//                    val tabMatch = tabsUpdated.find { it.status == tabStatus }
//                    val tabChanged = tabMatch != null
//
//                    if (tabChanged) {
//                        tab.text = "$tabStatus (${tabMatch!!.attendanceCount})"
//                    }
//                }
//            }
//        }
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

        viewModel.markUserCheckedIn(
            gigId = attendanceData.gigId,
            gigerId = attendanceData.gigerId,
            gigerName = attendanceData.gigerName,
            businessName = attendanceData.businessName
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
        //event
//        FirebaseAuth.getInstance().currentUser?.uid?.let {
//            val map = mapOf("Giger ID" to attendanceData.gigerId, "TL ID" to it, "Business Name" to attendanceData.businessName)
//            eventTracker.pushEvent(TrackingEventArgs("tl_attempted_decline",map))
//        }


        DeclineGigDialogFragment.launch(
            attendanceData.gigId,
            childFragmentManager,
            null,
            true
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