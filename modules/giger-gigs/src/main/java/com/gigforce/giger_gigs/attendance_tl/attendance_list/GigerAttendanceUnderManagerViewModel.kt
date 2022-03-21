package com.gigforce.giger_gigs.attendance_tl.attendance_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.datamodels.attendance.GigerAttedance
import com.gigforce.common_ui.datamodels.attendance.TlAttendance
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceType
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceStatus
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

    fun markUserCheckedIn(
        gigId: String
    ) = viewModelScope.launch {

        if (currentMarkingAttendanceForGigs.contains(gigId)) {
            logger.d(TAG, "already marking attendance for gig-id $gigId is in progress , no-op")
            return@launch
        }

        val currentlyMarkingAttendanceForGig = attendanceListRaw.find {
            it.id == gigId
        } ?: return@launch

        currentMarkingAttendanceForGigs.add(gigId)
        updateAttendanceMarkingStatusAndEmit(
            gigId,
            false
        )

        try {
            attendanceRepository.markCheckIn(
                gigId = gigId,
                imagePathInFirebase = null,
                latitude = null,
                longitude = null,
                markingAddress = null,
                locationFake = null,
                locationAccuracy = null
            )


            val currentLoggedInTLId = firebaseAuthStateListener.getCurrentSignInInfo()?.uid ?: "N/A"
//            val map = mapOf(
//                "Giger ID" to gigerId,
//                "TL ID" to currentLoggedInTLId,
//                "Business Name" to businessName
//            )
//            eventTracker.pushEvent(TrackingEventArgs("tl_marked_checkin", map))

            currentMarkingAttendanceForGigs.remove(gigId)
            updateAttendanceStatusInRawAndViewList(
                gigId,
                AttendanceStatus.PRESENT
            )
            processAttendanceListAndEmitToView(
                showDataUpdatedToast = false,
                updateStatusTabsCount = true
            )
        } catch (e: Exception) {

            if (currentMarkingAttendanceForGigs.contains(gigId)) {
                currentMarkingAttendanceForGigs.remove(gigId)
            }

            updateAttendanceMarkingStatusAndEmit(
                gigId,
                false
            )

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.ShowErrorUnableToMarkAttendanceForUser(
                    error = "Unable to mark Present of ${currentlyMarkingAttendanceForGig.gigerName}"
                )
            )
        }
    }

    private fun updateAttendanceStatusInRawAndViewList(
        gigId: String,
        status: String
    ) {
        attendanceListRaw.find {
            it.id == gigId
        }?.let {

            if (AttendanceType.OVERWRITE_BOTH == it.attendanceType) {

                val gigerAttendanceObj = it.gigerAttedance?.apply {
                    this.attendanceStatus =
                        if (status == AttendanceStatus.PRESENT) StatusFilters.ACTIVE else StatusFilters.INACTIVE
                    this.status = status
                    this.markedBy = "TL"
                    this.statusString = status
                    this.statusTextColorCode = it.getDefaultTextColorCodeForStatus(status)
                    this.statusBackgroundColorCode = it.getDefaultBackgroundColorCodeForStatus(status)
                } ?: GigerAttedance(
                    attendanceStatus = if (status == AttendanceStatus.PRESENT) StatusFilters.ACTIVE else StatusFilters.INACTIVE,
                    status = status,
                    markedBy = "TL",
                    statusString = status,
                    statusTextColorCode = it.getDefaultTextColorCodeForStatus(status),
                    statusBackgroundColorCode = it.getDefaultBackgroundColorCodeForStatus(status),
                    checkInTime = null,
                    checkOutTime = null,
                    checkInImage = null,
                    checkOutImage = null
                )
                it.gigerAttedance = gigerAttendanceObj
            } else {

                val tlAttendanceObj = it.tlAttendance?.apply {
                    this.attendanceStatus =
                        if (status == AttendanceStatus.PRESENT) StatusFilters.ACTIVE else StatusFilters.INACTIVE
                    this.status = status
                    this.statusString = status
                    this.statusTextColorCode = it.getDefaultTextColorCodeForStatus(status)
                    this.statusBackgroundColorCode = it.getDefaultBackgroundColorCodeForStatus(status)
                } ?: TlAttendance(
                    attendanceStatus = if (status == AttendanceStatus.PRESENT) StatusFilters.ACTIVE else StatusFilters.INACTIVE,
                    status = status,
                    statusString = status,
                    statusTextColorCode = it.getDefaultTextColorCodeForStatus(status),
                    statusBackgroundColorCode = it.getDefaultBackgroundColorCodeForStatus(status),
                    checkInTime = null,
                    checkOutTime = null,
                    checkInImage = null,
                    checkOutImage = null
                )
                it.tlAttendance = tlAttendanceObj
            }
        }
    }

    private fun updateConflictStatusInRawAndViewList(
        gigId: String,
        optionSelected: Boolean
    ) {
        attendanceListRaw.find {
            it.id == gigId
        }?.let {

            if (AttendanceType.OVERWRITE_BOTH == it.attendanceType) {

                if(optionSelected){

                    it.isResolved = true
                    val tlAttendanceCurrent = it.tlAttendance ?: return@let

                    val updatedTLAttendance = tlAttendanceCurrent.createNewCopyFromGigerAttendance(
                        it.gigerAttedance!!
                    )
                    it.tlAttendance = updatedTLAttendance
                } else{
                    it.isResolved = true
                }
            }
        }
    }




    fun markUserAbsent(
        gigId: String,
        reasonId: String,
        reason: String
    ) = viewModelScope.launch {

        if (currentMarkingAttendanceForGigs.contains(gigId)) {
            logger.d(TAG, "already marking attendance for gig-id $gigId is in progress , no-op")
            return@launch
        }

        val currentlyMarkingAttendanceForGig = attendanceListRaw.find {
            it.id == gigId
        } ?: return@launch

        currentMarkingAttendanceForGigs.add(gigId)
        updateAttendanceMarkingStatusAndEmit(
            gigId,
            false
        )

        try {
            attendanceRepository.markDecline(
                gigId,
                reasonId,
                reason
            )

            attendanceShownOnScreen.find {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData && it.gigId == gigId
            }?.let {
                (it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).apply {
                    status = GigAttendanceStatus.INACTIVE
                }

                val currentLoggedInTLId =
                    firebaseAuthStateListener.getCurrentSignInInfo()?.uid ?: "N/A"
                val gigerId = currentlyMarkingAttendanceForGig.gigerId ?: "N/A"
                val map = mapOf(
                    "Giger ID" to gigerId,
                    "TL ID" to currentLoggedInTLId,
                    "Business Name" to currentlyMarkingAttendanceForGig.getBusinessNameNN()
                )
                eventTracker.pushEvent(
                    TrackingEventArgs(
                        "tl_marked_checkin",
                        map
                    )
                )//todo change here
            }

            currentMarkingAttendanceForGigs.remove(gigId)
            updateAttendanceStatusInRawAndViewList(
                gigId,
                AttendanceStatus.ABSENT
            )
            updateAttendanceMarkingStatusAndEmit(
                gigId,
                true
            )
        } catch (e: Exception) {

            if (currentMarkingAttendanceForGigs.contains(gigId)) {
                currentMarkingAttendanceForGigs.remove(gigId)
            }

            updateAttendanceMarkingStatusAndEmit(
                gigId,
                false
            )

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.ShowErrorUnableToMarkAttendanceForUser(
                    error = "Unable to mark absent of ${currentlyMarkingAttendanceForGig.gigerName}"
                )
            )
        }
    }

    fun resolveAttendanceConflict(
        gigId: String,
        resolveId: String,
        optionSelected: Boolean
    ) = viewModelScope.launch {


        if (currentMarkingAttendanceForGigs.contains(gigId)) {
            logger.d(TAG, "already marking attendance for gig-id $gigId is in progress , no-op")
            return@launch
        }

        val currentlyMarkingAttendanceForGig = attendanceListRaw.find {
            it.id == gigId
        } ?: return@launch

        currentMarkingAttendanceForGigs.add(gigId)
        updateAttendanceMarkingStatusAndEmit(
            gigId,
            false
        )

        try {
            attendanceRepository.resolveAttendanceConflict(
                resolveId = resolveId,
                optionSelected = optionSelected
            )

            updateConflictStatusInRawAndViewList(
                gigId,
                optionSelected
            )

            currentMarkingAttendanceForGigs.remove(gigId)
            updateAttendanceMarkingStatusAndEmit(
                gigId,
                true
            )

            val currentLoggedInTLId =
                firebaseAuthStateListener.getCurrentSignInInfo()?.uid ?: "N/A"
            val gigerId = currentlyMarkingAttendanceForGig.gigerId ?: "N/A"
            val map = mapOf(
                "Giger ID" to gigerId,
                "TL ID" to currentLoggedInTLId,
                "Business Name" to currentlyMarkingAttendanceForGig.getBusinessNameNN()
            )
            eventTracker.pushEvent(
                TrackingEventArgs(
                    "tl_marked_checkin",
                    map
                )
            )//todo change here
        } catch (e: Exception) {

            if (currentMarkingAttendanceForGigs.contains(gigId)) {
                currentMarkingAttendanceForGigs.remove(gigId)
            }

            updateAttendanceMarkingStatusAndEmit(
                gigId,
                false
            )

            _viewEffects.emit(
                GigerAttendanceUnderManagerViewContract.UiEffect.ShowErrorUnableToMarkAttendanceForUser(
                    error = "Unable to mark absent of ${currentlyMarkingAttendanceForGig.gigerName}"
                )
            )
        }
    }

    private suspend fun updateAttendanceMarkingStatusAndEmit(
        gigId: String,
        updateStatusTabsCount: Boolean
    ) {

        attendanceShownOnScreen.find {
            it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                    it.gigId == gigId
        }?.run {
            val markingAttendanceForThisGig = currentMarkingAttendanceForGigs.contains(gigId)
            (this as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).currentlyMarkingAttendanceForThisGig =
                markingAttendanceForThisGig
        }

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
                showUpdateToast = false,
                tabsDataCounts = if (updateStatusTabsCount) prepareAttendanceTabsCountList() else null
            )
        )
    }

    private fun prepareAttendanceTabsCountList() = listOf(
        AttendanceStatusAndCountItemData(
            status = StatusFilters.ENABLED,
            attendanceCount = attendanceShownOnScreen.count{
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
