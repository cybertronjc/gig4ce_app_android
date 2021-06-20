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
import com.gigforce.core.CoreViewHolder
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.giger_gigs.dialogFragments.DeclineGigDialogFragment
import com.gigforce.giger_gigs.listItems.AttendanceGigerAttendanceRecyclerItemView
import com.gigforce.giger_gigs.models.AttendanceFilterItemShift
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.viewModels.AttendanceUnderManagerSharedViewState
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModel
import com.gigforce.giger_gigs.viewModels.GigerAttendanceUnderManagerViewModelState
import com.gigforce.giger_gigs.viewModels.SharedGigerAttendanceUnderManagerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
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


@AndroidEntryPoint
class GigersAttendanceUnderManagerFragment : Fragment() {

    private val swipeTouchListener = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        private var previousDx = 0f

        override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
        ): Int {
            if (viewHolder is CoreViewHolder && viewHolder.itemView is AttendanceGigerAttendanceRecyclerItemView) {

                try {
                    val gigData =
                            (viewHolder.itemView as AttendanceGigerAttendanceRecyclerItemView).getGigIdOrThrow()

                    return when {
                        "Present".equals(gigData.attendanceStatus, true) -> ItemTouchHelper.LEFT
                        "Declined".equals(gigData.attendanceStatus, true) -> ItemTouchHelper.RIGHT
                        else -> super.getSwipeDirs(recyclerView, viewHolder)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    return super.getSwipeDirs(recyclerView, viewHolder)
                }
            } else {
                return 0 //Disabling swipe for view type that are not attendance items
            }
        }

        override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun isItemViewSwipeEnabled(): Boolean = attendanceSwipeControlsEnabled

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (viewHolder is CoreViewHolder && viewHolder.itemView is AttendanceGigerAttendanceRecyclerItemView) {

                try {
                    val gigData =
                            (viewHolder.itemView as AttendanceGigerAttendanceRecyclerItemView).getGigIdOrThrow()

                    when (direction) {
                        ItemTouchHelper.LEFT -> showDeclineGigerAttendanceDialog(gigData.gigId)
                        ItemTouchHelper.RIGHT -> markUserAttendanceAsPresent(
                                gigData.gigId,
                                gigData.gigerName
                        )
                        else -> { /*Do Nothing*/
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            previousDx = 0f
        }

        override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
        ) {

            if (previousDx <= 0 && dX > 0) {
                // swiping from left to right

                val itemView = viewHolder.itemView
                val height = itemView.bottom.toFloat() - itemView.top.toFloat()
                val width = height / 5
                viewHolder.itemView.translationX = dX / 5
                val paint = Paint()
                paint.color = Color.parseColor("#D32F2F")
                val background = RectF(
                        itemView.right.toFloat() + dX / 5,
                        itemView.top.toFloat(),
                        itemView.right.toFloat(),
                        itemView.bottom.toFloat()
                )
                c.drawRect(background, paint)
//                val icon = BitmapFactory.decodeResource(resources, R.drawable.ic_checkin_illus)
//                val icon_dest = RectF((itemView.right + dX / 7), itemView.top.toFloat() + width, itemView.right.toFloat() + dX / 20, itemView.bottom.toFloat() - width)
//                c.drawBitmap(icon, null, icon_dest, paint)


            } else if (previousDx >= 0 && dX < 0) {
                // swiping from right to left


            }

            previousDx = dX
            super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX / 1.5f,
                    dY,
                    actionState,
                    isCurrentlyActive
            )
        }
    }


    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private lateinit var viewBinding: FragmentGigerUnderManagersAttendanceBinding
    private val simpleDateFormat = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())
    private val itemTouchHelper = ItemTouchHelper(swipeTouchListener)
    private var attendanceSwipeControlsEnabled = false

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
        initView()
        initViewModel()
        getAttendanceFor(LocalDate.now())
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

        lifecycleScope.launch {

            viewBinding.toolbar.apply {
                showTitle("Gigers Attendance")
                showSearchOption("Search Attendance")
                hideActionMenu()
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
            selectedSlotTv.text = "Today"
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
                    } else{
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
                    } else{
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
                                it.shouldEnableAttendanceControls,
                                it.attendanceItemData
                        )
                        is GigerAttendanceUnderManagerViewModelState.ErrorInLoadingDataFromServer -> errorInLoadingAttendanceFromServer(
                                it.error,
                                it.shouldShowErrorButton
                        )
                        GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer -> showDataLoadingFromServer()
                        GigerAttendanceUnderManagerViewModelState.NoAttendanceFound -> noAttendanceFound()
                        is GigerAttendanceUnderManagerViewModelState.FiltersUpdated -> updateFilters(
                                it.shouldRemoveOlderStatusTabs,
                                it.attendanceStatuses,
                                it.business,
                                it.shiftTimings
                        )
                        is GigerAttendanceUnderManagerViewModelState.UserMarkedPresent -> {
                            showSnackBar(it.message)
                        }
                        is GigerAttendanceUnderManagerViewModelState.ErrorWhileMarkingUserPresent -> {
                            showErrorInMarkingPresent(it.error)
                        }
                    }
                })

        sharedGigViewModel.attendanceUnderManagerSharedViewState
                .observe(viewLifecycleOwner, {

                    when (it) {
                        is AttendanceUnderManagerSharedViewState.GigDeclined -> {
                            showSnackBar("User Marked Absent")
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
                .setTitle("Unable to mark present")
                .setMessage(error)
                .setPositiveButton("Okay") { _, _ -> }
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
                                    shiftTimeForView = "Select Shift"
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
                            add(0, "Select Company")
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
            enableAttendanceSwipeControls: Boolean,
            attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        attendanceSwipeControlsEnabled = enableAttendanceSwipeControls
        this.gigersUnderManagerMainLayout.errorInfoLayout.gone()
        toolbar.showSearchOption("Search Attendance")
        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()

            stopShimmer(
                    this.statusShimmerContainer as LinearLayout,
                    R.id.chip_like_shimmer_controller
            )
            stopShimmer(
                    this.attendanceShimmerContainer as LinearLayout,
                    R.id.shimmer_controller
            )
            this.attendanceRecyclerView.collection = attendanceItemData
            if (attendanceItemData.isEmpty()) noAttendanceFound()

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
            for (i in 0 until this.statusTabLayout.tabCount) {

                val tab = this.statusTabLayout.getTabAt(i)
                val tabStatus = tab!!.tag.toString()

                val attendanceStatusCount =
                        attendanceStatuses.find { it.status == tabStatus }?.attendanceCount
                                ?: 0
                tab.text = "$tabStatus ($attendanceStatusCount)"
            }
        }


    }


    private fun errorInLoadingAttendanceFromServer(
            error: String,
            shouldShowRetryButton: Boolean
    ) = viewBinding.apply {

        stopShimmer(
                this.gigersUnderManagerMainLayout.attendanceShimmerContainer as LinearLayout,
                R.id.shimmer_controller
        )
        stopShimmer(
                this.gigersUnderManagerMainLayout.statusShimmerContainer as LinearLayout,
                R.id.chip_like_shimmer_controller
        )

        this.gigersUnderManagerMainLayout.errorInfoLayout.visible()
        this.gigersUnderManagerMainLayout.gigersUnderManagerMainError.text = error
    }


    private fun noAttendanceFound() = viewBinding.gigersUnderManagerMainLayout.apply {

        stopShimmer(
                this.statusShimmerContainer as LinearLayout,
                R.id.chip_like_shimmer_controller
        )
        stopShimmer(
                this.attendanceShimmerContainer as LinearLayout,
                R.id.shimmer_controller
        )

        this.attendanceRecyclerView.collection = emptyList()
        this.errorInfoLayout.visible()
        this.gigersUnderManagerMainError.text = "No Attendance Found"
    }


    private fun showDataLoadingFromServer() = viewBinding.apply {

        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()
            this.errorInfoLayout.gone()

            toolbar.hideSearchOption()
            this.statusTabLayout.removeAllTabs()
            startShimmer(
                    this.statusShimmerContainer as LinearLayout,
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
                    this.attendanceShimmerContainer as LinearLayout,
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


    private fun markUserAttendanceAsPresent(
            gigId: String,
            userName: String
    ) = viewModel.markUserCheckedIn(
            gigId,
            userName
    )


    private fun showDeclineGigerAttendanceDialog(gigId: String) = DeclineGigDialogFragment.launch(
            gigId,
            childFragmentManager,
            null,
            true
    )


    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
                activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
    }

    companion object {
        const val TAG = "GigerAttendanceUnderManagerFrg"
    }

}