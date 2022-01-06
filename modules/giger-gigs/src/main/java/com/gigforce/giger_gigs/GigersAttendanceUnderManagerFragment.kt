package com.gigforce.giger_gigs

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.TextView
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
import com.gigforce.core.extensions.getTextChangeAsStateFlow
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.giger_gigs.databinding.FragmentGigerUnderManagersAttendanceBinding
import com.gigforce.giger_gigs.dialogFragments.DeclineGigBottomSheetDialogFragment
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.viewModels.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.jaeger.library.StatusBarUtil
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


    private val sharedGigViewModel: SharedGigerAttendanceUnderManagerViewModel by activityViewModels()
    private val viewModel: GigerAttendanceUnderManagerViewModel by viewModels()
    private lateinit var viewBinding: FragmentGigerUnderManagersAttendanceBinding

    @Inject
    lateinit var navigation: INavigation

    private val swipeTouchHandler = AttendanceSwipeHandler(this)
    private val itemTouchHelper = ItemTouchHelper(swipeTouchHandler)

    val changeTLSelectionIds = arrayListOf<String>()
    var changeTLSelections : HashMap<String, Boolean>? = HashMap<String, Boolean>()

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault())

    private val datePicker: DatePickerDialog by lazy {
        val cal = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker?, year: Int, month: Int, dayOfMonth: Int ->
                viewModel.fetchUsersAttendanceDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog
    }

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
        initToolbar()
        initTabLayout()
        initView()
        initListeners()
        initViewModel()
        getAttendanceFor(LocalDate.now())
    }

    private fun initListeners() = viewBinding.apply {
        gigersUnderManagerMainLayout.changeTeamLeader.setOnClickListener {
            navigation.navigateTo(
                "gig/changeTeamLeaderBottomSheet"
            )
        }
    }

    private fun initToolbar() = viewBinding.toolbar.apply {
        if (title.isNotBlank())
            setAppBarTitle(title)
        else
            setAppBarTitle("Giger attendance")

        setSubTitle(dateFormatter.format(LocalDate.now()))
        changeBackButtonDrawable()
        makeBackgroundMoreRound()
        //   makeTitleBold()
        setBackButtonListener {
            activity?.onBackPressed()
        }
        lifecycleScope.launch {
            search_item.getTextChangeAsStateFlow()
                .debounce(300)
                .distinctUntilChanged()
                .flowOn(Dispatchers.Default)
                .collect { searchString ->

                    Log.d("Search ", "Searhcingg...$searchString")
                    viewModel.searchAttendance(searchString)
                }
        }

        filterImageButton.setImageResource(R.drawable.ic_calendar_white)
        filterImageButton.setOnClickListener {
            datePicker.show()
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
    private fun initView() {

        viewBinding.gigersUnderManagerMainLayout.apply {

            attendanceRecyclerView.layoutManager = LinearLayoutManager(requireContext())
            itemTouchHelper.attachToRecyclerView(attendanceRecyclerView)

            statusTabLayout.onTabSelected { tab ->
                val selectedTab = tab ?: return@onTabSelected
                viewModel.filterAttendanceByStatus(
                    selectedTab.tag.toString()
                )
            }
        }
    }

    private fun initTabLayout() = viewBinding.gigersUnderManagerMainLayout.apply {
        val betweenSpace = 70

        val slidingTabStrip: ViewGroup = statusTabLayout.getChildAt(0) as ViewGroup
        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
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
                        it.date,
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
                it.attendanceStatuses
            )
        })

        viewModel.changeTLSelectionsMap.observe(viewLifecycleOwner, {
            setChangeTlSelection(it)
        })

        viewModel.markAttendanceState.observe(viewLifecycleOwner, {

            when (it) {
                is GigerAttendanceUnderManagerViewModelMarkAttendanceState.ErrorWhileMarkingUserPresent -> showErrorInMarkingPresent(
                    it.error
                )
                is GigerAttendanceUnderManagerViewModelMarkAttendanceState.UserMarkedPresent -> showSnackBar(
                    it.message,
                    true
                )
                is GigerAttendanceUnderManagerViewModelMarkAttendanceState.UserMarkedAbsent -> showSnackBar(
                    it.message,
                    false
                )
            }
        })

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {

            sharedGigViewModel.attendanceUnderManagerSharedViewState
                .collect{

                    when (it) {
                        is AttendanceUnderManagerSharedViewState.GigDeclined -> {
                            showSnackBar(getString(R.string.user_marked_absent_giger_gigs), false)
                            viewModel.gigDeclinedUpdateGigerStatusInView(it.gigId)
                        }
                        else -> {
                        }
                    }
                }
        }
    }

    private fun setChangeTlSelection(hashMap: HashMap<String, Boolean>?) = viewBinding.apply{
        val count = hashMap?.size
        if (count != null) {
            if (count > 0){
                toolbar.setAppBarTitle("$count Selected")
                gigersUnderManagerMainLayout.changeTeamLeader.visible()
                changeTLSelections = hashMap
            }else{
                toolbar.setAppBarTitle(title)
                gigersUnderManagerMainLayout.changeTeamLeader.gone()
                changeTLSelections?.clear()
            }
        }

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
    ) {

        if (attendanceStatuses != null)
            setStatusTabs(shouldRemoveOlderStatusTabs, attendanceStatuses)

    }

    private fun showSnackBar(
        text: String,
        isSuccessMessage: Boolean
    ) {
        Snackbar.make(
            viewBinding.root,
            text,
            Snackbar.LENGTH_LONG
        ).apply {

            if (isSuccessMessage) {
                view.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.attendance_status_green_alpha_50,
                        null
                    )
                )

                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(
                        Color.parseColor("#EBF8EC")
                    )
            } else {
                view.setBackgroundColor(
                    ResourcesCompat.getColor(
                        resources,
                        R.color.attendance_status_pink_alpha_50,
                        null
                    )
                )

                view.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
                    .setTextColor(
                        Color.parseColor("#FEE8E8")
                )
            }
        }.show()
    }

    private fun showStatusAndAttendanceOnView(
        date: LocalDate,
        attendanceSwipeControlsEnabled: Boolean,
        enablePresentSwipeAction: Boolean,
        enableDeclineSwipeAction: Boolean,
        attendanceItemData: List<AttendanceRecyclerItemData>
    ) = viewBinding.apply {

        toolbar.setSubTitle(dateFormatter.format(date))
        swipeTouchHandler.attendanceSwipeControlsEnabled = attendanceSwipeControlsEnabled
        swipeTouchHandler.markPresentSwipeActionEnabled = enablePresentSwipeAction
        swipeTouchHandler.declineSwipeActionEnabled = enableDeclineSwipeAction

        stopShimmer(
            this.attendanceShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )

        this.errorInfoLayout.gone()
        this.gigersUnderManagerMainLayout.apply {
            this.root.visible()
            this.swipeLabel.isVisible = attendanceSwipeControlsEnabled
            addViewModelToBusinessLabel(attendanceItemData)

            this.attendanceRecyclerView.collection = attendanceItemData
            if (attendanceItemData.isEmpty()) {
                noAttendanceFound()
            }
        }
    }

    private fun addViewModelToBusinessLabel(
        attendanceItemData: List<AttendanceRecyclerItemData>
    ) {

        attendanceItemData.onEach {

            if (it is AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData) {
                it.gigAttendanceViewModel = viewModel
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

        initTabLayout()
    }


    private fun errorInLoadingAttendanceFromServer(
        error: String,
        shouldShowRetryButton: Boolean
    ) = viewBinding.apply {

        stopShimmer(
            this.attendanceShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )

        this.gigersUnderManagerMainLayout.swipeLabel.gone()
        this.errorInfoLayout.visible()
        this.gigersUnderManagerMainError.text = error
    }


    private fun noAttendanceFound() = viewBinding.apply {
        stopShimmer(
            this.attendanceShimmerContainer as LinearLayout,
            R.id.shimmer_controller
        )

        this.gigersUnderManagerMainLayout.attendanceRecyclerView.collection = emptyList()
        this.errorInfoLayout.visible()
    }


    private fun showDataLoadingFromServer() = viewBinding.apply {

        this.errorInfoLayout.gone()
        this.gigersUnderManagerMainLayout.apply {
            this.root.gone()
        }

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
            attendanceData.gigerName
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

        DeclineGigBottomSheetDialogFragment.launch(
            attendanceData.gigId,
            childFragmentManager,
            null,
            true
        )
    }

}