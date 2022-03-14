package com.gigforce.giger_gigs.attendance_tl.attendance_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
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
    private val logger: GigforceLogger
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
    }

    private fun attendanceItemClicked(
        attendance: AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData
    ) = viewModelScope.launch {

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

    private fun fetchUsersAttendanceDate(
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

        _viewState.emit(
            GigerAttendanceUnderManagerViewContract.State.ShowOrUpdateAttendanceListOnView(
                date = currentlyFetchingForDate,
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = attendanceShownOnScreen,
                showUpdateToast = showDataUpdatedToast,
                tabsDataCounts = if (updateStatusTabsCount) prepareAttendanceTabsCountList() else null
            )
        )
    }

    private fun filterAttendanceByStatus(
        status: String
    ) = viewModelScope.launch(Dispatchers.IO) {

        currentlySelectedStatus = status
        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = false
        )
    }

    private fun searchAttendance(
        searchTerm: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlySearchTerm = searchTerm
        processAttendanceListAndEmitToView(
            showDataUpdatedToast = false,
            updateStatusTabsCount = true
        )
    }

    fun markUserCheckedIn(
        gigId: String,
        gigerId : String,
        gigerName: String,
        businessName: String
    ) = viewModelScope.launch {

        if (currentMarkingAttendanceForGigs.contains(gigId)) {
            logger.d(TAG, "already marking attendance for gig-id $gigId is in progress , no-op")
            return@launch
        }

        currentMarkingAttendanceForGigs.add(gigId)
        updateAttendanceMarkingStatusAndEmit(
            gigId,
            false
        )

        try {
            gigersAttendanceRepository.markUserAttendanceAsPresent(
                gigId
            )
            attendanceShownOnScreen.find {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData && it.gigId == gigId
            }?.let {
                (it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).status =
                    "Active"
            }

            val currentLoggedInTLId =
                firebaseAuthStateListener.getCurrentSignInInfo()?.uid ?: "N/A"
            val map = mapOf(
                "Giger ID" to gigerId,
                "TL ID" to currentLoggedInTLId,
                "Business Name" to businessName
            )
            eventTracker.pushEvent(TrackingEventArgs("tl_marked_checkin", map))

            processAttendanceListAndEmitToView(
                showDataUpdatedToast = false,
                updateStatusTabsCount = true
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
                    error = "Unable to mark Present of $gigerName"
                )
            )
        }
    }


    fun markUserAbsent(
        gigId: String,
        gigerId: String,
        reason: String,
        gigerName: String,
        businessName: String,
    ) = viewModelScope.launch {

        if (currentMarkingAttendanceForGigs.contains(gigId)) {
            logger.d(TAG, "already marking attendance for gig-id $gigId is in progress , no-op")
            return@launch
        }

        currentMarkingAttendanceForGigs.add(gigId)
        updateAttendanceMarkingStatusAndEmit(
            gigId,
            false
        )

        try {
            gigersAttendanceRepository.markUserAttendanceAsPresent(//todo change this
                gigId
            )
            attendanceShownOnScreen.find {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData && it.gigId == gigId
            }?.let {
                (it as AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData).apply {
                    status = GigAttendanceStatus.INACTIVE
                }

                val currentLoggedInTLId =
                    firebaseAuthStateListener.getCurrentSignInInfo()?.uid ?: "N/A"
                val map = mapOf(
                    "Giger ID" to gigerId,
                    "TL ID" to currentLoggedInTLId,
                    "Business Name" to businessName
                )
                eventTracker.pushEvent(TrackingEventArgs("tl_marked_checkin", map))//todo change here
            }

            currentMarkingAttendanceForGigs.remove(gigId)
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
                    error = "Unable to mark absent of $gigerName"
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

        _viewState.emit(
            GigerAttendanceUnderManagerViewContract.State.ShowOrUpdateAttendanceListOnView(
                date = currentlyFetchingForDate,
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = attendanceShownOnScreen,
                showUpdateToast = false,
                tabsDataCounts = if (updateStatusTabsCount) prepareAttendanceTabsCountList() else null
            )
        )
    }

    private fun prepareAttendanceTabsCountList() = listOf(
        AttendanceStatusAndCountItemData(
            status = StatusFilters.ENABLED,
            attendanceCount = attendanceShownOnScreen.count(),
            statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
        ),
        AttendanceStatusAndCountItemData(
            status = StatusFilters.ACTIVE,
            attendanceCount = attendanceShownOnScreen.count {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                        it.status == StatusFilters.ACTIVE
            },
            statusSelected = currentlySelectedStatus == StatusFilters.ACTIVE
        ),
        AttendanceStatusAndCountItemData(
            status = StatusFilters.INACTIVE,
            attendanceCount = attendanceShownOnScreen.count {
                it is AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData &&
                        it.status == StatusFilters.INACTIVE
            },
            statusSelected = currentlySelectedStatus == StatusFilters.INACTIVE
        ),
    )

}
