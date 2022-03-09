package com.gigforce.giger_gigs.attendance_tl

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.giger_gigs.models.AttendanceFilterItemShift
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.repositories.GigersAttendanceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.*
import javax.inject.Inject

sealed class GigerAttendanceUnderManagerViewModelState {

    object LoadingDataFromServer : GigerAttendanceUnderManagerViewModelState()

    object NoAttendanceFound : GigerAttendanceUnderManagerViewModelState()

    data class ErrorInLoadingDataFromServer(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : GigerAttendanceUnderManagerViewModelState()

    data class AttendanceDataLoaded(
        val attendanceSwipeControlsEnabled: Boolean,
        val enablePresentSwipeAction: Boolean,
        val enableDeclineSwipeAction: Boolean,
        val attendanceItemData: List<AttendanceRecyclerItemData>,
        val showUpdateToast: Boolean
    ) : GigerAttendanceUnderManagerViewModelState()
}


sealed class GigerAttendanceUnderManagerViewModelMarkAttendanceState {

    data class UserMarkedPresent(
        val message: String
    ) : GigerAttendanceUnderManagerViewModelMarkAttendanceState()

    data class ErrorWhileMarkingUserPresent(
        val error: String
    ) : GigerAttendanceUnderManagerViewModelMarkAttendanceState()

}

data class AttendanceFilters(
    val shouldRemoveOlderStatusTabs: Boolean,
    val attendanceStatuses: List<AttendanceStatusAndCountItemData>?
)

@HiltViewModel
class GigerAttendanceUnderManagerViewModel @Inject constructor(
    private val gigersAttendanceRepository: GigersAttendanceRepository
) : ViewModel() {

    companion object {
        const val TAG = "GigerAttendanceUnderManagerViewModel"
    }

    @Inject
    lateinit var eventTracker: IEventTracker

    /* data*/
    private var attendanceListRaw: List<GigAttendanceApiModel> = emptyList()
    private var attendanceShownOnScreen: MutableList<AttendanceRecyclerItemData> = mutableListOf()

    //Filters
    private var currentlyFetchingForDate: LocalDate = LocalDate.now()
    private var currentlySelectedStatus: String = StatusFilters.ENABLED
    private var currentlySearchTerm: String? = null
    private var collapsedBusinessList: MutableList<String> = mutableListOf()

    //To view Observables
    private val _gigerAttendanceUnderManagerViewState =
        MutableLiveData<GigerAttendanceUnderManagerViewModelState>()
    val gigerAttendanceUnderManagerViewState: LiveData<GigerAttendanceUnderManagerViewModelState> =
        _gigerAttendanceUnderManagerViewState

    private val _filters = MutableLiveData<AttendanceFilters>()
    val filters: LiveData<AttendanceFilters> = _filters

    private val _markAttendanceState =
        MutableLiveData<GigerAttendanceUnderManagerViewModelMarkAttendanceState>()
    val markAttendanceState: LiveData<GigerAttendanceUnderManagerViewModelMarkAttendanceState> =
        _markAttendanceState

    init {
        fetchUsersAttendanceDate(LocalDate.now())
    }

    fun fetchUsersAttendanceDate(
        date: LocalDate
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlyFetchingForDate = date

        _gigerAttendanceUnderManagerViewState.postValue(
            GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer
        )

        try {
            val gigersAttendance = gigersAttendanceRepository.getAttendance(
                date
            )
            attendanceListRaw = gigersAttendance
            processAttendanceListAndEmitToView(true)
        } catch (e: Exception) {
            _gigerAttendanceUnderManagerViewState.postValue(
                GigerAttendanceUnderManagerViewModelState.ErrorInLoadingDataFromServer(
                    error = e.message ?: "Error while loading attendance",
                    shouldShowErrorButton = true
                )
            )

            CrashlyticsLogger.e(
                TAG,
                "fetching gigers attendance",
                e
            )
        }
    }

    private fun processAttendanceListAndEmitToView(
        showDataUpdatedToast : Boolean
    ) {
        attendanceShownOnScreen =
            AttendanceUnderTLListDataProcessor.processAttendanceListAndFilters(
                attendance = attendanceListRaw,
                collapsedBusiness = collapsedBusinessList,
                currentlySelectedStatus = currentlySelectedStatus,
                currentlySearchTerm = currentlySearchTerm,
                gigerAttendanceUnderManagerViewModel = this@GigerAttendanceUnderManagerViewModel
            ).toMutableList()

        _gigerAttendanceUnderManagerViewState.postValue(
            GigerAttendanceUnderManagerViewModelState.AttendanceDataLoaded(
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = attendanceShownOnScreen,
                showUpdateToast = showDataUpdatedToast
            )
        )
    }

    fun filterAttendanceByStatus(
        status: String
    ) = viewModelScope.launch(Dispatchers.IO) {

        currentlySelectedStatus = status
        processAttendanceListAndEmitToView(false)
    }

    fun searchAttendance(
        searchTerm: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlySearchTerm = searchTerm
        processAttendanceListAndEmitToView(false)
    }


    private fun resetAllFilters() {
        currentlySearchTerm = null
//        currentlySelectedStatus = null
    }

    fun markUserCheckedIn(
        gigId: String,
        userName: String,
        gigerId: String
    ) = viewModelScope.launch {

        try {
            gigersAttendanceRepository.markUserAttendanceAsPresent(
                gigId
            )
            attendanceListRaw.find {
                gigId == it.id
            }?.let {
//                it.attendanceStatus = "Present"
//                it.gigStatus = GigStatus.ONGOING.getStatusString()
//                //event
//                FirebaseAuth.getInstance().currentUser?.uid?.let {
//                    val map = mapOf("Giger ID" to gigerId , "TL ID" to it, "Business Name" to businessName)
//                    eventTracker.pushEvent(TrackingEventArgs("tl_marked_checkin",map))
//                }
            }

            _markAttendanceState.postValue(
                GigerAttendanceUnderManagerViewModelMarkAttendanceState.UserMarkedPresent(
                    "$userName marked present"
                )
            )
            delay(300)
            updateStatusCounts()
            delay(300)
//            filterCachedResultsAndEmit()
        } catch (e: Exception) {
            CrashlyticsLogger.e(TAG, "while marking present in tl", e)
            _markAttendanceState.postValue(
                GigerAttendanceUnderManagerViewModelMarkAttendanceState.ErrorWhileMarkingUserPresent(
                    error = "Error while marking $userName present"
                )
            )
        }
    }

    fun gigDeclinedUpdateGigerStatusInView(
        gigId: String
    ) = viewModelScope.launch {

//        attendanceListRaw?.find {
//            gigId == it.gigId
//        }?.let {
//            it.attendanceStatus = "Declined"
//            it.gigStatus = GigStatus.DECLINED.getStatusString()
//        }
        updateStatusCounts()
        delay(300)
//        filterCachedResultsAndEmit()
    }

    private fun updateStatusCounts() {
        val gigAttendanceData = AttendanceUnderTLListDataProcessor.filterAttendanceList(
            attendance = attendanceListRaw,
            currentlySelectedStatus = currentlySelectedStatus,
            currentlySearchTerm = currentlySearchTerm
        )


        val statuses = listOf(
            AttendanceStatusAndCountItemData(
                status = StatusFilters.ENABLED,
                attendanceCount = gigAttendanceData.count(),
                statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
            ),
            AttendanceStatusAndCountItemData(
                status = StatusFilters.ACTIVE,
                attendanceCount = gigAttendanceData.count(),//todo
                statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
            ),
            AttendanceStatusAndCountItemData(
                status = StatusFilters.ENABLED,
                attendanceCount = gigAttendanceData.count(),
                statusSelected = currentlySelectedStatus == StatusFilters.ENABLED
            ),
        )

        _filters.postValue(
            AttendanceFilters(
                shouldRemoveOlderStatusTabs = false,
                attendanceStatuses = statuses,
            )
        )
    }

}
