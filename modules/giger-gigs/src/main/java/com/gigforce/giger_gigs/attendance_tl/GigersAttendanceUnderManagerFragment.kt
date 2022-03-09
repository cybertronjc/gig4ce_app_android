package com.gigforce.giger_gigs.attendance_tl

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
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.datamodels.ShimmerDataModel
import com.gigforce.common_ui.ext.onTabSelected
import com.gigforce.common_ui.ext.startShimmer
import com.gigforce.common_ui.ext.stopShimmer
import com.gigforce.core.AppConstants
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.R
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
    AttendanceSwipeHandler.AttendanceSwipeHandlerListener, IOnBackPressedOverride {

    companion object {
        const val TAG = "GigerAttendanceUnderManagerFrg"
    }

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

        viewModel.filters.observe(viewLifecycleOwner) {

            updateFilters(
                it.shouldRemoveOlderStatusTabs,
                it.attendanceStatuses
            )
        }

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
        attendanceStatuses: List<AttendanceStatusAndCountItemData>?
    ) {

        if (attendanceStatuses != null)
            setStatusTabs(shouldRemoveOlderStatusTabs, attendanceStatuses)

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
        attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        swipeTouchHandler.attendanceSwipeControlsEnabled = attendanceSwipeControlsEnabled
        swipeTouchHandler.markPresentSwipeActionEnabled = enablePresentSwipeAction
        swipeTouchHandler.declineSwipeActionEnabled = enableDeclineSwipeAction

    }

    private fun setStatusTabs(
        shouldRemoveOlderStatusTabs: Boolean,
        attendanceStatuses: List<AttendanceStatusAndCountItemData>
    ) = viewBinding.apply {

        if (shouldRemoveOlderStatusTabs) {
            this.tablayout.removeAllTabs()

            attendanceStatuses.forEach {

                val newTab = this.tablayout.newTab().apply {
                    this.text = "${it.status} (${it.attendanceCount})"
                    this.tag = it.status
                }
                this.tablayout.addTab(newTab)
            }
        } else {
            //Just updating Tabs Text
            val currentShownTabsInView = mutableListOf<String>()
            for (i in 0 until this.tablayout.tabCount) {

                val tab = this.tablayout.getTabAt(i)
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

                val newTab = this.tablayout.newTab().apply {
                    this.text = "${it.status} (${it.attendanceCount})"
                    this.tag = it.status
                }
                this.tablayout.addTab(newTab)
            }

            for (i in 0 until tablayout.tabCount) {

                val tab = tablayout.getTabAt(i)
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

    }


    private fun noAttendanceFound() = viewBinding.apply {

//        stopShimmer(
//            this.statusShimmerContainer,
//            R.id.chip_like_shimmer_controller
//        )
//        stopShimmer(
//            this.attendanceShimmerContainer,
//            R.id.shimmer_controller
//        )
//
//        this.attendanceRecyclerView.collection = emptyList()
//        this.errorInfoLayout.visible()
//        this.gigersUnderManagerMainError.text = getString(R.string.no_attendance_found_giger_gigs)
//
//        this.businessSpinner.gone()
//        this.businessLabel.gone()
//        this.shiftSpinner.gone()
//        this.shiftLabel.gone()
    }


    private fun showDataLoadingFromServer() = viewBinding.apply {

    }

    private fun hideSoftKeyboard() {

        val activity = activity ?: return

        val inputMethodManager =
            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(activity.currentFocus?.windowToken, 0)
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
            attendanceData.gigId,
            attendanceData.gigerName,
            attendanceData.gigerId
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
        return  false

    }

}