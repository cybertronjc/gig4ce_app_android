package com.gigforce.giger_gigs.attendance_tl.attendance_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceType
import com.gigforce.core.IEventTracker
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.repositories.GigersAttendanceRepository
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
) : ViewModel() {

    companion object {
        const val TAG = "GigerAttendanceUnderManagerViewModel"
    }

    /* data*/
    private var attendanceListRaw: List<GigAttendanceApiModel> = emptyList()
    private var attendanceShownOnScreen: MutableList<AttendanceRecyclerItemData> = mutableListOf()

    //Filters
    private var currentlyFetchingForDate: LocalDate = LocalDate.now()
    private var currentlySelectedStatus: String = StatusFilters.ENABLED
    private var currentlySearchTerm: String? = null
    private var collapsedBusinessList: MutableList<String> = mutableListOf()
    private var currentMarkingAttendanceForGigs: MutableSet<String> = mutableSetOf()

    //To view Observables
    private val _viewState = MutableStateFlow<GigerAttendanceUnderManagerViewContract.State>(
        GigerAttendanceUnderManagerViewContract.State.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<GigerAttendanceUnderManagerViewContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    init {
        fetchUsersAttendanceDate(LocalDate.now())
    }

    fun handleEvent(
        event: GigerAttendanceUnderManagerViewContract.UiEvent
    ) = when (event) {
        is GigerAttendanceUnderManagerViewContract.UiEvent.AttendanceItemClicked -> attendanceItemClicked(
            event.attendance
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.BusinessHeaderClicked -> businessHeaderClicked(
            event.header
        )
        GigerAttendanceUnderManagerViewContract.UiEvent.RefreshAttendanceClicked -> fetchUsersAttendanceDate(
            currentlyFetchingForDate
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.FiltersApplied.DateChanged -> fetchUsersAttendanceDate(
            event.date
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.FiltersApplied.SearchTextChanged -> searchAttendance(
            event.searchText
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.FiltersApplied.TabChanged -> filterAttendanceByStatus(
            event.tab
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.AttendanceItemResolveClicked -> resolveButtonClicked(
            event.attendance
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.UserRightSwipedForMarkingPresent -> userRightSwipedForMarkingPresent(
            event.attendance
        )
        is GigerAttendanceUnderManagerViewContract.UiEvent.UserLeftSwipedForMarkingAbsent -> userLeftSwipedForMarkingAbsent(
            event.attendance
        )
    }

    private fun userRightSwipedForMarkingPresent(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        val rightSwipedAttendance = attendanceListRaw.find {
            it.id == attendance.gigId
        } ?: return@launch

        if (rightSwipedAttendance.attendanceType.isNullOrBlank() || AttendanceType.OVERWRITE_BOTH == rightSwipedAttendance.attendanceType) {

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkGigerActiveConfirmation(
                    gigId = attendance.gigId,
                    hasGigerMarkedHimselfInActive = false
                )
            )
        } else {
            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkGigerActiveConfirmation(
                    gigId = attendance.gigId,
                    hasGigerMarkedHimselfInActive = attendance.gigerAttendanceStatus == AttendanceStatus.ABSENT
                )
            )
        }
    }

    private fun userLeftSwipedForMarkingAbsent(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        val leftSwipedAttendance = attendanceListRaw.find {
            it.id == attendance.gigId
        } ?: return@launch

        if (leftSwipedAttendance.getGigerMarkedAttendance() == AttendanceStatus.PRESENT) {

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkInactiveConfirmationDialog(
                    gigId = attendance.gigId
                )
            )
        } else {

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.OpenMarkInactiveSelectReasonDialog(
                    gigId = attendance.gigId
                )
            )
        }
    }

    private fun resolveButtonClicked(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {

        _viewEffects.emit(
            GigerAttendanceUnderManagerViewContract.UiEffect.ShowResolveAttendanceConflictScreen(
                gigId = attendance.gigId,
                gigAttendanceData = attendance.mapToGigAttendance()
            )
        )
    }

    private fun attendanceItemClicked(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {
        _viewEffects.emit(
            GigerAttendanceUnderManagerViewContract.UiEffect.ShowGigerDetailsScreen(
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

        if (_viewState.value is GigerAttendanceUnderManagerViewContract.State.LoadingAttendanceList) {
            return@launch
        }

        currentlyFetchingForDate = date
        _viewState.emit(
            GigerAttendanceUnderManagerViewContract.State.LoadingAttendanceList(null)
        )

        try {
            val gigersAttendance = gigersAttendanceRepository.getAttendance(
                date
            )
            attendanceListRaw = gigersAttendance
            processAttendanceListAndEmitToView(
                showDataUpdatedToast = true,
                updateStatusTabsCount = true
            )
        } catch (e: Exception) {

            if (e is IOException) {
                _viewState.emit(
                    GigerAttendanceUnderManagerViewContract.State.ErrorInLoadingOrUpdatingAttendanceList(
                        e.message ?: "Unable to fetch gigers attendance"
                    )
                )
            } else {
                _viewState.emit(
                    GigerAttendanceUnderManagerViewContract.State.ErrorInLoadingOrUpdatingAttendanceList(
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
        attendanceShownOnScreen =
            AttendanceUnderTLListDataProcessor.processAttendanceListAndFilters(
                attendance = attendanceListRaw,
                collapsedBusiness = collapsedBusinessList,
                currentMarkingAttendanceForGigs = currentMarkingAttendanceForGigs,
                currentlySelectedStatus = currentlySelectedStatus,
                currentlySearchTerm = currentlySearchTerm,
                gigerAttendanceUnderManagerViewModel = this@GigerAttendanceUnderManagerViewModel
            ).toMutableList()

        val listToEmitCopy = attendanceShownOnScreen.map {

            if (it is AttendanceRecyclerItemData.AttendanceBusinessHeaderItemData) {
                it.copy()
            } else {
                (it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).copy()
            }
        }.toMutableList()

        _viewState.emit(
            GigerAttendanceUnderManagerViewContract.State.ShowOrUpdateAttendanceListOnView(
                date = currentlyFetchingForDate,
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = listToEmitCopy,
                showUpdateToast = showDataUpdatedToast,
                tabsDataCounts = if (updateStatusTabsCount) prepareAttendanceTabsCountList() else null
            )
        )
    }

    fun filterAttendanceByStatus(
        status: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        if (_viewState.value is GigerAttendanceUnderManagerViewContract.State.LoadingAttendanceList) {
            return@launch
        }

        currentlySelectedStatus = status
        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = false
        )
    }

    fun searchAttendance(
        searchTerm: String
    ) = viewModelScope.launch(Dispatchers.IO) {

        if (_viewState.value is GigerAttendanceUnderManagerViewContract.State.LoadingAttendanceList) {
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

    fun updateAttendanceStatusInRawListAndEmit(
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

    private fun prepareAttendanceTabsCountList() = listOf(
        AttendanceStatusAndCountItemData(
            status = StatusFilters.ENABLED,
            attendanceCount = attendanceShownOnScreen.count {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
            },
            statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
        ),
        AttendanceStatusAndCountItemData(
            status = StatusFilters.ACTIVE,
            attendanceCount = attendanceShownOnScreen.count {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                        it.status == AttendanceStatus.PRESENT
            },
            statusSelected = currentlySelectedStatus == StatusFilters.ACTIVE
        ),
        AttendanceStatusAndCountItemData(
            status = StatusFilters.INACTIVE,
            attendanceCount = attendanceShownOnScreen.count {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                        it.status == AttendanceStatus.ABSENT
            },
            statusSelected = currentlySelectedStatus == StatusFilters.INACTIVE
        ),
    )
}
