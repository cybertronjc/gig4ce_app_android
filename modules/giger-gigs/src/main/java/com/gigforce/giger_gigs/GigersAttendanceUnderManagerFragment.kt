package com.gigforce.giger_gigs

import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.models.AttendanceFilterItemShift
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.viewModels.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jaeger.library.StatusBarUtil
import com.prolificinteractive.materialcalendarview.CalendarDay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class GigersAttendanceUnderManagerFragment : Fragment(),
    AttendanceSwipeHandler.AttendanceSwipeHandlerListener {

    @Inject
    lateinit var eventTracker: IEventTracker

    @Inject
    lateinit var navigation: INavigation

    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private lateinit var viewBinding: FragmentGigerUnderManagersAttendanceBinding
    private val simpleDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

    private val swipeTouchHandler = AttendanceSwipeHandler(this)
    private val itemTouchHelper = ItemTouchHelper(swipeTouchHandler)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentGigerUnderManagersAttendanceBinding.inflate(
            inflater,
            container,
            false
        )
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        initView()
        initViewModel()
        getAttendanceFor(LocalDate.now())
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
    private fun initView() {

        viewBinding.toolbar.setBackButtonListener {
            if (viewBinding.toolbar.isSearchCurrentlyShown) {
                hideSoftKeyboard()
            } else {
                activity?.onBackPressed()
            }
        }

        viewBinding.gigersUnderManagerMainLayout.joinNowButton.setOnClickListener {
            navigation.navigateTo("LeadMgmt/selectionForm1", bundleOf(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_ATTENDANCE to true
            ))
        }


        lifecycleScope.launch {

            viewBinding.toolbar.apply {

                if (title.isNotBlank())
                    showTitle(title)
                else
                    showTitle(context.getString(R.string.gigers_attendance_giger_gigs))
                hideActionMenu()
                showSearchOption(context.getString(R.string.search_name_giger_gigs))
                viewBinding.toolbar.hideSubTitle()
                getSearchTextChangeAsFlow()
                    .debounce(300)
                    .distinctUntilChanged()
                    .flowOn(Dispatchers.Default)
                    .collect { searchString ->
                        Log.d("Search ", "Searhcingg...$searchString")
                        viewModel.searchAttendance(searchString)
                    }
            }
        }

        viewBinding.gigersUnderManagerMainLayout.apply {


            attendanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(attendanceRecyclerView)
            slotContainer.setOnClickListener {

                slotCalendar.isVisible = !slotCalendar.isVisible
            }
            selectedSlotTv.text = getString(R.string.today_giger_gigs)
            slotCalendar.selectedDate = CalendarDay.today()
            slotCalendar.setOnDateChangedListener { _, date, _ ->

                val selectedDate = LocalDate.of(date.year, date.month, date.day)
                selectedSlotTv.text = selectedDate.format(simpleDateFormat)
                slotCalendar.gone()

                viewModel.fetchUsersAttendanceDate(selectedDate)
            }

            statusTabLayout.onTabSelected { tab ->
                val selectedTab = tab ?: return@onTabSelected
                viewModel.filterAttendanceByStatus(
                    if (selectedTab.tag.toString() == "All") null else selectedTab.tag.toString()
                )
            }

            businessSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (businessSpinner.childCount != 0 && businessSpinner.selectedItemPosition != 0) {
                        viewModel.filterAttendanceByBusiness(businessSpinner.selectedItem.toString())
                    } else {
                        viewModel.filterAttendanceByBusiness(null)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

            shiftSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    if (shiftSpinner.childCount != 0 && shiftSpinner.selectedItemPosition != 0) {

                        val selectedShift = shiftSpinner.selectedItem as AttendanceFilterItemShift
                        viewModel.filterDataByShift(selectedShift.shift)
                    } else {
                        viewModel.filterDataByShift(null)
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

    }

    private fun getAttendanceFor(date: LocalDate) {
        viewModel.fetchUsersAttendanceDate(date)
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(), ResourcesCompat.getColor(
                resources,
                R.color.lipstick_two,
                null
            )
        )
    }

    private fun initViewModel() {

        viewModel.gigerAttendanceUnderManagerViewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    is GigerAttendanceUnderManagerViewModelState.AttendanceDataLoaded -> showStatusAndAttendanceOnView(
                        it.attendanceSwipeControlsEnabled,
                        it.enablePresentSwipeAction,
                        it.enableDeclineSwipeAction,
                        it.attendanceItemData
                    )
                    is GigerAttendanceUnderManagerViewModelState.ErrorInLoadingDataFromServer -> errorInLoadingAttendanceFromServer(
                        it.error,
                        it.shouldShowErrorButton
                    )
                    GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer -> showDataLoadingFromServer()
                    GigerAttendanceUnderManagerViewModelState.NoAttendanceFound -> noAttendanceFound()
                }
            })

        viewModel.filters.observe(viewLifecycleOwner, {

            updateFilters(
                it.shouldRemoveOlderStatusTabs,
                it.attendanceStatuses,
                it.business,
                it.shiftTimings
            )
        })

        viewModel.markAttendanceState.observe(viewLifecycleOwner, {

            when (it) {
                is GigerAttendanceUnderManagerViewModelMarkAttendanceState.ErrorWhileMarkingUserPresent -> showErrorInMarkingPresent(
                    it.error
                )
                is GigerAttendanceUnderManagerViewModelMarkAttendanceState.UserMarkedPresent -> {
                    showSnackBar(
                        it.message
                    )
                }
            }
        })

        sharedGigViewModel.attendanceUnderManagerSharedViewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    is AttendanceUnderManagerSharedViewState.GigDeclined -> {
                        showSnackBar(getString(R.string.user_marked_absent_giger_gigs))
                        viewModel.gigDeclinedUpdateGigerStatusInView(it.gigId)
                    }
                    else -> {
                    }
                }
            })
    }

    private fun showErrorInMarkingPresent(
        error: String
    ) {

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.unable_to_mark_present_giger_gigs))
            .setMessage(error)
            .setPositiveButton(getString(R.string.okay_giger_gigs)) { _, _ -> }
            .show()
    }

    private fun updateFilters(
        shouldRemoveOlderStatusTabs: Boolean,
        attendanceStatuses: List<AttendanceStatusAndCountItemData>?,
        business: List<String>?,
        shiftTimings: List<AttendanceFilterItemShift>?
    ) {

        if (attendanceStatuses != null)
            setStatusTabs(shouldRemoveOlderStatusTabs, attendanceStatuses)

        if (business != null) {
            setBusinessOnSpinners(business)
        }

        if (shiftTimings != null) {
            setShiftTimmingsOnSpinners(shiftTimings)
        }
    }

    private fun setShiftTimmingsOnSpinners(
        shiftTimings: List<AttendanceFilterItemShift>
    ) {

        val shiftAdapter: ArrayAdapter<AttendanceFilterItemShift> =
            ArrayAdapter<AttendanceFilterItemShift>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                shiftTimings.toMutableList().apply {
                    add(
                        0, AttendanceFilterItemShift(
                            shift = "",
                            shiftTimeForView = getString(R.string.select_shift_giger_gigs)
                        )
                    )
                }
            )
        shiftAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.gigersUnderManagerMainLayout.shiftSpinner.adapter = shiftAdapter
    }

    private fun setBusinessOnSpinners(
        business: List<String>
    ) {

        val businessAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                business.toMutableList().apply {
                    add(0, getString(R.string.select_company_giger_gigs))
                }
            )
        businessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.gigersUnderManagerMainLayout.businessSpinner.adapter = businessAdapter
    }

    private fun showSnackBar(
        text: String
    ) {
        Snackbar.make(
            viewBinding.gigersUnderManagerMainLayout.root,
            text,
            Snackbar.LENGTH_SHORT
        ).show()
    }

    private fun showStatusAndAttendanceOnView(
        attendanceSwipeControlsEnabled: Boolean,
        enablePresentSwipeAction: Boolean,
        enableDeclineSwipeAction: Boolean,
        attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        swipeTouchHandler.attendanceSwipeControlsEnabled = attendanceSwipeControlsEnabled
        swipeTouchHandler.markPresentSwipeActionEnabled = enablePresentSwipeAction
        swipeTouchHandler.declineSwipeActionEnabled = enableDeclineSwipeAction

        this.gigersUnderManagerMainLayout.errorInfoLayout.gone()
        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()
            this.swipeLabel.isVisible = attendanceSwipeControlsEnabled

            stopShimmer(
                this.statusShimmerContainer,
                R.id.chip_like_shimmer_controller
            )
            stopShimmer(
                this.attendanceShimmerContainer,
                R.id.shimmer_controller
            )
            this.attendanceRecyclerView.collection = attendanceItemData
            if (attendanceItemData.isEmpty()) {
                noAttendanceFound()
            } else {
                this.businessSpinner.visible()
                this.businessLabel.visible()
                this.shiftSpinner.visible()
                this.shiftLabel.visible()
            }
        }
    }

    private fun setStatusTabs(
        shouldRemoveOlderStatusTabs: Boolean,
        attendanceStatuses: List<AttendanceStatusAndCountItemData>
    ) = viewBinding.gigersUnderManagerMainLayout.apply {

        if (shouldRemoveOlderStatusTabs) {
            this.statusTabLayout.removeAllTabs()

            attendanceStatuses.forEach {

                val newTab = this.statusTabLayout.newTab().apply {
                    this.text = "${it.status} (${it.attendanceCount})"
                    this.tag = it.status
                }
                this.statusTabLayout.addTab(newTab)
            }
        } else {
            //Just updating Tabs Text
            val currentShownTabsInView = mutableListOf<String>()
            for (i in 0 until this.statusTabLayout.tabCount) {

                val tab = this.statusTabLayout.getTabAt(i)
                val tabStatus = tab!!.tag.toString()
                currentShownTabsInView.add(tabStatus)
            }

            val tabsAdded = mutableListOf<AttendanceStatusAndCountItemData>()
            val tabsDeleted = mutableListOf<String>()
            val tabsUpdated = mutableListOf<AttendanceStatusAndCountItemData>()

            attendanceStatuses.forEach {

                if (!currentShownTabsInView.contains(it.status)) {
                    tabsAdded.add(it)
                } else {
                    tabsUpdated.add(it)
                }
            }

            currentShownTabsInView.forEach { tabShown ->

                if (attendanceStatuses.find { it.status == tabShown } == null)
                    tabsDeleted.add(tabShown)
            }

            tabsAdded.forEach {

                val newTab = this.statusTabLayout.newTab().apply {
                    this.text = "${it.status} (${it.attendanceCount})"
                    this.tag = it.status
                }
                this.statusTabLayout.addTab(newTab)
            }

            for (i in 0 until statusTabLayout.tabCount) {

                val tab = statusTabLayout.getTabAt(i)
                val tabStatus = tab!!.tag.toString()

                if (tabsDeleted.contains(tabStatus)) {
                    tab.text = "$tabStatus (0)"
                } else {
                    val tabMatch = tabsUpdated.find { it.status == tabStatus }
                    val tabChanged = tabMatch != null

                    if (tabChanged) {
                        tab.text = "$tabStatus (${tabMatch!!.attendanceCount})"
                    }
                }
            }
        }
    }


    private fun errorInLoadingAttendanceFromServer(
        error: String,
        shouldShowRetryButton: Boolean
    ) = viewBinding.apply {

        stopShimmer(
            this.gigersUnderManagerMainLayout.attendanceShimmerContainer,
            R.id.shimmer_controller
        )
        stopShimmer(
            this.gigersUnderManagerMainLayout.statusShimmerContainer,
            R.id.chip_like_shimmer_controller
        )

        this.gigersUnderManagerMainLayout.swipeLabel.gone()
        this.gigersUnderManagerMainLayout.errorInfoLayout.visible()
        this.gigersUnderManagerMainLayout.gigersUnderManagerMainError.text = error
    }


    private fun noAttendanceFound() = viewBinding.gigersUnderManagerMainLayout.apply {

        stopShimmer(
            this.statusShimmerContainer,
            R.id.chip_like_shimmer_controller
        )
        stopShimmer(
            this.attendanceShimmerContainer,
            R.id.shimmer_controller
        )

        this.attendanceRecyclerView.collection = emptyList()
        this.errorInfoLayout.visible()
        this.gigersUnderManagerMainError.text = getString(R.string.no_attendance_found_giger_gigs)

        this.businessSpinner.gone()
        this.businessLabel.gone()
        this.shiftSpinner.gone()
        this.shiftLabel.gone()
    }


    private fun showDataLoadingFromServer() = viewBinding.apply {

        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()
            this.errorInfoLayout.gone()

            this.swipeLabel.gone()
//            toolbar.hideSearchOption()
            this.statusTabLayout.removeAllTabs()
            startShimmer(
                this.statusShimmerContainer,
                ShimmerDataModel(
                    cardRes = com.gigforce.common_ui.R.layout.shimmer_chip_like_layout,
                    minHeight = R.dimen.size_30,
                    minWidth = R.dimen.size_90,
                    marginRight = R.dimen.size_8,
                    marginTop = R.dimen.size16,
                    marginLeft = R.dimen.size_1,
                    itemsToBeDrawn = 3,
                    orientation = LinearLayout.HORIZONTAL
                ),
                R.id.chip_like_shimmer_controller
            )

            this.attendanceRecyclerView.collection = emptyList()
            startShimmer(
                this.attendanceShimmerContainer,
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

    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    companion object {
        const val TAG = "GigerAttendanceUnderManagerFrg"
    }

    override fun onRightSwipedForMarkingPresent(
        viewHolder: RecyclerView.ViewHolder,
        attendanceData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {
        viewBinding.gigersUnderManagerMainLayout.attendanceRecyclerView.coreAdapter.notifyItemChanged(
            viewHolder.adapterPosition
        )
        itemTouchHelper.startSwipe(viewHolder)

        viewModel.markUserCheckedIn(
            attendanceData.gigId,
            attendanceData.gigerName,
            attendanceData.gigerId,
            attendanceData.businessName
        )
    }

    override fun onLeftSwipedForDecliningAttendance(
        viewHolder: RecyclerView.ViewHolder,
        attendanceData: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) {
        viewBinding.gigersUnderManagerMainLayout.attendanceRecyclerView.coreAdapter.notifyItemChanged(
            viewHolder.adapterPosition
        )
        itemTouchHelper.startSwipe(viewHolder)
        //event
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            val map = mapOf("Giger ID" to attendanceData.gigerId, "TL ID" to it, "Business Name" to attendanceData.businessName)
            eventTracker.pushEvent(TrackingEventArgs("tl_attempted_decline",map))
        }

        DeclineGigDialogFragment.launch(
            attendanceData.gigId,
            childFragmentManager,
            null,
            true
        )
    }

}