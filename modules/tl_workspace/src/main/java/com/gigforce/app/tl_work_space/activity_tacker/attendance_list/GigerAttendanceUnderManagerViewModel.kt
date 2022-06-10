package com.gigforce.app.tl_work_space.activity_tacker.attendance_list

import androidx.lifecycle.viewModelScope
import com.gigforce.app.data.repositoriesImpl.gigs.AttendanceStatus
import com.gigforce.app.data.repositoriesImpl.gigs.GigAttendanceApiModel
import com.gigforce.app.data.repositoriesImpl.gigs.GigersAttendanceRepository
import com.gigforce.app.tl_work_space.BaseTLWorkSpaceViewModel
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import com.gigforce.app.tl_work_space.activity_tacker.SharedAttendanceTLSharedViewModelEvents
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceRecyclerItemData
import com.gigforce.app.tl_work_space.activity_tacker.models.AttendanceTabData
import com.gigforce.app.tl_work_space.custom_tab.CustomTabClickListener
import com.gigforce.app.tl_work_space.custom_tab.CustomTabData
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewModel
import com.gigforce.common_ui.viewmodels.gig.SharedGigViewState
import com.gigforce.core.IEventTracker
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject


@HiltViewModel
class GigerAttendanceUnderManagerViewModel @Inject constructor(
    private val gigersAttendanceRepository: GigersAttendanceRepository,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private var eventTracker: IEventTracker,
    private val logger: GigforceLogger,
    private val attendanceRepository: GigAttendanceRepository
) : BaseTLWorkSpaceViewModel<
        GigerAttendanceUnderManagerViewEvents,
        GigerAttendanceUnderManagerViewState,
        GigerAttendanceUnderManagerViewEffect>(initialState = GigerAttendanceUnderManagerViewState.ScreenLoaded),
    CustomTabClickListener {

    companion object {
        const val TAG = "GigerAttendanceUnderManagerViewModel"
    }

    /* data*/
    private var attendanceListRaw: MutableList<GigAttendanceApiModel> = mutableListOf()
    private var statusMaster: List<AttendanceTabData> = emptyList()
    private var attendanceShownOnScreen: MutableList<AttendanceRecyclerItemData> = mutableListOf()

    //Filters
    private var currentlyFetchingForDate: LocalDate = LocalDate.now()
    private var currentlySelectedStatus: String = StatusFilters.ENABLED
    private var currentlySearchTerm: String? = null
    private var collapsedBusinessList: MutableList<String> = mutableListOf()
    private var currentMarkingAttendanceForGigs: MutableSet<String> = mutableSetOf()

    //SharedViewModels
    private lateinit var gigsJoiningSharedViewModel: SharedGigViewModel
    private lateinit var gigsSharedViewModel: AttendanceTLSharedViewModel


    //To view Observables
    private val _viewState = MutableStateFlow<GigerAttendanceUnderManagerViewState>(
        GigerAttendanceUnderManagerViewState.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<GigerAttendanceUnderManagerViewEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    init {
        fetchUsersAttendanceDate(LocalDate.now())
    }

    fun setGigsJoiningSharedViewModel(
        sharedViewModel: SharedGigViewModel
    ) = viewModelScope.launch {
        this@GigerAttendanceUnderManagerViewModel.gigsJoiningSharedViewModel = sharedViewModel
        this@GigerAttendanceUnderManagerViewModel.gigsJoiningSharedViewModel
            .gigSharedViewModelState.collect {

                when (it) {
                    is SharedGigViewState.TeamLeaderOfGigerChangedWithGigId -> removeGigFromCurrentlyShownGigs(
                        it.gigId
                    )
                    is SharedGigViewState.UserDroppedWithGig -> fetchUsersAttendanceDate(
                        currentlyFetchingForDate
                    )
                    else -> {}
                }
            }
    }

    fun setGigsSharedViewModel(
        sharedViewModel: AttendanceTLSharedViewModel
    ) = viewModelScope.launch {
        this@GigerAttendanceUnderManagerViewModel.gigsSharedViewModel = sharedViewModel
        this@GigerAttendanceUnderManagerViewModel.gigsSharedViewModel
            .sharedEvents
            .collect {

                when (it) {
                    is SharedAttendanceTLSharedViewModelEvents.AttendanceUpdated -> updateAttendanceStatusInRawListAndEmit(
                        it.attendance
                    )
                    is SharedAttendanceTLSharedViewModelEvents.OpenMarkInactiveReasonsDialog -> _viewEffects.emit(
                        GigerAttendanceUnderManagerViewEffect.OpenMarkInactiveSelectReasonDialog(
                            gigId = it.gigId,
                            popConfirmationDialog = true
                        )
                    )
                }
            }
    }


    override fun handleEvent(
        event: GigerAttendanceUnderManagerViewEvents
    ) {
        when (event) {
            is GigerAttendanceUnderManagerViewEvents.AttendanceItemClicked -> attendanceItemClicked(
                event.attendance
            )
            is GigerAttendanceUnderManagerViewEvents.BusinessHeaderClicked -> businessHeaderClicked(
                event.header
            )
            GigerAttendanceUnderManagerViewEvents.RefreshAttendanceClicked -> fetchUsersAttendanceDate(
                currentlyFetchingForDate
            )
            is GigerAttendanceUnderManagerViewEvents.FiltersApplied.DateChanged -> fetchUsersAttendanceDate(
                event.date
            )
            is GigerAttendanceUnderManagerViewEvents.FiltersApplied.SearchTextChanged -> searchAttendance(
                event.searchText
            )
            is GigerAttendanceUnderManagerViewEvents.AttendanceItemResolveClicked -> resolveButtonClicked(
                event.attendance
            )
            is GigerAttendanceUnderManagerViewEvents.UserRightSwipedForMarkingPresent -> userRightSwipedForMarkingPresent(
                event.attendance
            )
            is GigerAttendanceUnderManagerViewEvents.UserLeftSwipedForMarkingAbsent -> userLeftSwipedForMarkingAbsent(
                event.attendance
            )
        }
    }

    private fun userRightSwipedForMarkingPresent(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        val rightSwipedAttendance = attendanceListRaw.find {
            it.id == attendance.gigId
        } ?: return@launch

        _viewEffects.emit(
            GigerAttendanceUnderManagerViewEffect.OpenMarkGigerActiveConfirmation(
                gigId = attendance.gigId,
                hasGigerMarkedHimselfInActive = rightSwipedAttendance.getGigerMarkedAttendance() == AttendanceStatus.ABSENT
            )
        )
    }

    private fun userLeftSwipedForMarkingAbsent(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        val leftSwipedAttendance = attendanceListRaw.find {
            it.id == attendance.gigId
        } ?: return@launch

        if (leftSwipedAttendance.getGigerMarkedAttendance() == AttendanceStatus.PRESENT) {

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewEffect.OpenMarkInactiveConfirmationDialog(
                    gigId = attendance.gigId
                )
            )
        } else {

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewEffect.OpenMarkInactiveSelectReasonDialog(
                    gigId = attendance.gigId
                )
            )
        }
    }

    private fun resolveButtonClicked(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {

        _viewEffects.emit(
            GigerAttendanceUnderManagerViewEffect.ShowResolveAttendanceConflictScreen(
                gigId = attendance.gigId,
                gigAttendanceData = attendance.mapToGigAttendance()
            )
        )
    }

    private fun attendanceItemClicked(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        _viewEffects.emit(
            GigerAttendanceUnderManagerViewEffect.ShowGigerDetailsScreen(
                attendance.gigId,
                attendance.mapToGigAttendance()
            )
        )
    }

    private fun businessHeaderClicked(
        header: AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData
    ) = viewModelScope.launch {

        if (collapsedBusinessList.contains(header.businessName)) {
            collapsedBusinessList.remove(header.businessName)
        } else {
            collapsedBusinessList.add(header.businessName)
        }

        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = false
        )
    }

    fun fetchUsersAttendanceDate(
        date: LocalDate
    ) = viewModelScope.launch(Dispatchers.IO) {

        if (_viewState.value is GigerAttendanceUnderManagerViewState.LoadingAttendanceList) {
            return@launch
        }

        currentlyFetchingForDate = date
        _viewState.emit(
            GigerAttendanceUnderManagerViewState.LoadingAttendanceList(null)
        )

        try {
            val gigersAttendance = gigersAttendanceRepository.getAttendance(
                date
            )
            attendanceListRaw = gigersAttendance.gigers.toMutableList()
            statusMaster = gigersAttendance.statusCount.map {
                AttendanceTabData(
                    id = it.id ?: "",
                    title = it.title ?: "",
                    value = it.count ?: 0,
                    selected = false,
                    valueChangedBy = it.valueChangedBy ?: 0,
                    changeType = ValueChangeType.fromChangeString(it.valueChangeType),
                    viewModel = this@GigerAttendanceUnderManagerViewModel
                )
            }

            processAttendanceListAndEmitToView(
                showDataUpdatedToast = true,
                updateStatusTabsCount = true
            )
        } catch (e: Exception) {

            if (e is IOException) {
                _viewState.emit(
                    GigerAttendanceUnderManagerViewState.ErrorInLoadingOrUpdatingAttendanceList(
                        e.message ?: "Unable to fetch gigers attendance"
                    )
                )
            } else {
                _viewState.emit(
                    GigerAttendanceUnderManagerViewState.ErrorInLoadingOrUpdatingAttendanceList(
                        "Unable to fetch gigers attendance"
                    )
                )

                logger.e(
                    TAG,
                    "fetching gigers attendance",
                    e
                )
            }
        }
    }

    private suspend fun processAttendanceListAndEmitToView(
        showDataUpdatedToast: Boolean,
        updateStatusTabsCount: Boolean
    ) {
        val attendanceToStatusWithCountPair =
            AttendanceUnderTLListDataProcessor.processAttendanceListAndFilters(
                attendance = attendanceListRaw,
                collapsedBusiness = collapsedBusinessList,
                currentMarkingAttendanceForGigs = currentMarkingAttendanceForGigs,
                currentlySelectedStatus = currentlySelectedStatus,
                currentlySearchTerm = currentlySearchTerm,
                prepareAttendanceStatusAndCount = updateStatusTabsCount,
                gigerAttendanceUnderManagerViewModel = this@GigerAttendanceUnderManagerViewModel,
                statusMaster
            )
        attendanceShownOnScreen = attendanceToStatusWithCountPair.first.toMutableList()

        val listToEmitCopy = attendanceShownOnScreen.map {

            if (it is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData) {
                it.copy()
            } else {
                (it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).copy()
            }
        }.toMutableList()

        _viewState.emit(
            GigerAttendanceUnderManagerViewState.ShowOrUpdateAttendanceListOnView(
                date = currentlyFetchingForDate,
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = listToEmitCopy,
                showUpdateToast = showDataUpdatedToast,
                tabsDataCounts = attendanceToStatusWithCountPair.second
            )
        )
    }

    fun filterAttendanceByStatus(
        status: CustomTabData
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlySelectedStatus = status.tabId

        if (_viewState.value is GigerAttendanceUnderManagerViewState.LoadingAttendanceList) {
            return@launch
        }
        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = true
        )
    }

    fun searchAttendance(
        searchTerm: String
    ) = viewModelScope.launch(Dispatchers.IO) {

        if (_viewState.value is GigerAttendanceUnderManagerViewState.LoadingAttendanceList) {
            return@launch
        }

        currentlySearchTerm = searchTerm
        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = true
        )
    }


    /**
     * Attendance
     * 1. Check-in
     * 2. absent
     * 3. resolve conflict
     */

    private fun updateAttendanceStatusInRawListAndEmit(
        updatedGigData: GigAttendanceApiModel
    ) = viewModelScope.launch {
        val gigId = updatedGigData.id ?: return@launch

        if (currentMarkingAttendanceForGigs.contains(gigId))
            currentMarkingAttendanceForGigs.remove(gigId)

        val mutableAttendanceList = attendanceListRaw.toMutableList()
        val itemToReplaceIndex = mutableAttendanceList.indexOfFirst {
            it.id == updatedGigData.id
        }
        if (itemToReplaceIndex == -1) {
            return@launch
        }

        mutableAttendanceList[itemToReplaceIndex] = updatedGigData
        attendanceListRaw = mutableAttendanceList

        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = true
        )
    }

    private fun removeGigFromCurrentlyShownGigs(
        gigId: String
    ) = viewModelScope.launch {

        if (currentMarkingAttendanceForGigs.contains(gigId))
            currentMarkingAttendanceForGigs.remove(gigId)

        val mutableAttendanceList = attendanceListRaw.toMutableList()
        val itemToReplaceIndex = mutableAttendanceList.indexOfFirst {
            it.id == gigId
        }
        if (itemToReplaceIndex == -1) {
            return@launch
        }

        mutableAttendanceList.removeAt(itemToReplaceIndex)
        attendanceListRaw = mutableAttendanceList

        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = true
        )
    }

    override fun handleCustomTabClick(
        tabClickedType1: CustomTabData
    ) {
        filterAttendanceByStatus(tabClickedType1)
    }

    override fun gigerDropped(
        gigerId: String,
        jobProfileId: String
    ) {
        super.gigerDropped(gigerId, jobProfileId)

        viewModelScope.launch {

            attendanceListRaw.removeIf {
                gigerId == it.gigerId && jobProfileId == it.jobProfileId
            }

            processAttendanceListAndEmitToView(
                showDataUpdatedToast = true,
                updateStatusTabsCount = true
            )
        }
    }

    override fun teamLeaderChangedOf(
        gigerId: String,
        jobProfileId: String
    ) {
        super.teamLeaderChangedOf(gigerId, jobProfileId)
        viewModelScope.launch {

            attendanceListRaw.removeIf {
                gigerId == it.gigerId && jobProfileId == it.jobProfileId
            }
            processAttendanceListAndEmitToView(
                showDataUpdatedToast = true,
                updateStatusTabsCount = true
            )
        }
    }

}