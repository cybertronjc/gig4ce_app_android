package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.GigStatus
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.giger_gigs.models.AttendanceFilterItemShift
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.repositories.GigersAttendanceRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
        val attendanceItemData: List<AttendanceRecyclerItemData>
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
    val attendanceStatuses: List<AttendanceStatusAndCountItemData>?,
    val business: List<String>?,
    val shiftTimings: List<AttendanceFilterItemShift>?
)

@HiltViewModel
class GigerAttendanceUnderManagerViewModel @Inject constructor(
    private val gigersAttendanceRepository: GigersAttendanceRepository
) : ViewModel() {


    @Inject
    lateinit var eventTracker: IEventTracker

    /* data*/
    private var currentlyShownAttendanceData: List<GigerAttendance>? = null

    //Filters
    private var currentlyFetchingForDate: LocalDate = LocalDate.now()
    private var currentlySelectedStatus: String? = null
    private var currentlySelectedBusiness: String? = null
    private var currentlySelectedShiftTime: String? = null
    private var currentlySearchTerm: String? = null
    private var areTabsDirty: Boolean = false

    private var timeFormat24Hour: SimpleDateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private var timeFormat12Hour: SimpleDateFormat =
        SimpleDateFormat("hh:mm a", Locale.getDefault())

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


    fun fetchUsersAttendanceDate(
        date: LocalDate
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlyFetchingForDate = date
        areTabsDirty = true

        _gigerAttendanceUnderManagerViewState.postValue(
            GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer
        )

        try {
            delay(200)
            val gigersAttendance = gigersAttendanceRepository.getAttendance(
                date
            )
            resetAllFilters()
            currentlyShownAttendanceData = gigersAttendance

            if (gigersAttendance.isEmpty()) {
                _gigerAttendanceUnderManagerViewState.postValue(
                    GigerAttendanceUnderManagerViewModelState.NoAttendanceFound
                )
            } else {
                prepareAllFilters()
                filterCachedResultsAndEmit()
            }
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

    private fun prepareStatusAndAttendanceItems(
        gigersAttendance: List<GigerAttendance>
    ): List<AttendanceRecyclerItemData> {
9011190111

        val attendanceGroupedByShiftAndCompany = groupAttendanceByShiftTimeAndCompany(
            gigersAttendance
        )
        return mapGroupedAttendanceForRecyclerView(
            attendanceGroupedByShiftAndCompany
        )
    }

    private fun mapGroupedAttendanceForRecyclerView(
        attendanceGroupedByShiftAndCompany: List<Map<String, Map<String, List<GigerAttendance>>>>
    ): MutableList<AttendanceRecyclerItemData> {
        val attendanceRecyclerItemData: MutableList<AttendanceRecyclerItemData> = mutableListOf()
        attendanceGroupedByShiftAndCompany.forEach {

            it.forEach { (shiftTime, companyToAttendanceGroup) ->

                companyToAttendanceGroup.forEach { (companyName, gigerAttendance) ->
                    attendanceRecyclerItemData.add(
                        AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessAndShiftNameData(
                            businessName = companyName,
                            shiftName = formatShiftTime(shiftTime)
                        )
                    )

                    gigerAttendance.forEach {
                        attendanceRecyclerItemData.add(
                            mapRemoteGigerAttendanceToRecyclerViewAttendance(it, companyName)
                        )
                    }
                }
            }
        }

        return attendanceRecyclerItemData
    }

    private fun formatShiftTime(shiftTime: String): String {
        if (shiftTime.isBlank()) return ""
        if (!shiftTime.contains("-")) return shiftTime

        val shiftTimes = shiftTime.split("-")
        return buildString {

            try {
                val time1 = timeFormat24Hour.parse(shiftTimes[0].trim())
                val timeIn12Hours = timeFormat12Hour.format(time1)
                append(timeIn12Hours)
                append("-")

                val time2 = timeFormat24Hour.parse(shiftTimes[1].trim())
                val time2In12Hours = timeFormat12Hour.format(time2)
                append(time2In12Hours)
            } catch (e: Exception) {
                append(shiftTime)
            }
        }
    }

    private fun groupAttendanceByShiftTimeAndCompany(gigersAttendance: List<GigerAttendance>): List<Map<String, Map<String, List<GigerAttendance>>>> {

        return gigersAttendance.filter {
            it.companyName != null && it.shiftTime != null
        }.groupBy {
            it.shiftTime!!
        }.map { shiftToAttendanceGroup ->
            mapOf(shiftToAttendanceGroup.key to shiftToAttendanceGroup.value.groupBy { it.companyName!! })
        }
    }

    private fun prepareAttendanceStatusMasterAndTheirCount(
        gigersAttendance: List<GigerAttendance>
    ): List<AttendanceStatusAndCountItemData> {

        val statusList = gigersAttendance.filter {
            it.attendanceStatus != null
        }.distinctBy {
            it.attendanceStatus
        }.map { distinctStatus ->
            AttendanceStatusAndCountItemData(
                status = distinctStatus.attendanceStatus!!,
                attendanceCount = gigersAttendance.count { it.attendanceStatus == distinctStatus.attendanceStatus },
                statusSelected = distinctStatus.attendanceStatus!! == currentlySelectedStatus
            )
        }.toMutableList()


        statusList.add(
            0, AttendanceStatusAndCountItemData(
                status = "All",
                attendanceCount = gigersAttendance.size,
                statusSelected = currentlySelectedStatus == null
            )
        )


        return statusList
    }

    private fun mapRemoteGigerAttendanceToRecyclerViewAttendance(
        gigerAttendance: GigerAttendance,
        companyName: String
    ): AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData {

        return AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData(
            attendanceStatus = gigerAttendance.attendanceStatus!!,
            gigId = gigerAttendance.gigId ?: "",
            gigerId = gigerAttendance.uid!!,
            gigerName = gigerAttendance.name ?: "Name: NA",
            gigerPhoneNumber = gigerAttendance.phoneNumber ?: "",
            gigerDesignation = gigerAttendance.role ?: "Role: NA",
            gigerImage = gigerAttendance.profilePicture ?: "",
            gigStatus = gigerAttendance.gigStatus ?: "",
            gigerOffice = gigerAttendance.location ?: "",
            businessName = companyName ?: ""

        )
    }

    fun filterAttendanceByStatus(
        status: String?
    ) = viewModelScope.launch(Dispatchers.IO) {

        currentlySelectedStatus = status
        filterCachedResultsAndEmit()
    }

    fun filterAttendanceByBusiness(
        business: String?
    ) = viewModelScope.launch(Dispatchers.IO) {

        currentlySelectedBusiness = business
        updateSlotFilter()
        filterCachedResultsAndEmit()
    }

    fun filterDataByShift(
        shiftName: String?
    ) = viewModelScope.launch(Dispatchers.IO) {

        currentlySelectedShiftTime = shiftName
        filterCachedResultsAndEmit()
    }

    fun searchAttendance(
        searchTerm: String
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlySearchTerm = searchTerm
        filterCachedResultsAndEmit()
    }

    private suspend fun filterCachedResultsAndEmit() {
        if (currentlyShownAttendanceData == null) {
            return
        }

        if (currentlyShownAttendanceData!!.isEmpty()) {
            _gigerAttendanceUnderManagerViewState.postValue(
                GigerAttendanceUnderManagerViewModelState.NoAttendanceFound
            )
            return
        }


        val filteredAttendanceData = filterAttendanceData()
        val attendanceData = prepareStatusAndAttendanceItems(
            filteredAttendanceData
        )

        _gigerAttendanceUnderManagerViewState.postValue(
            GigerAttendanceUnderManagerViewModelState.AttendanceDataLoaded(
                attendanceSwipeControlsEnabled = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                enablePresentSwipeAction = currentlyFetchingForDate == LocalDate.now(),
                enableDeclineSwipeAction = !currentlyFetchingForDate.isBefore(LocalDate.now()),
                attendanceItemData = attendanceData
            )
        )
    }

    private fun filterAttendanceData(): List<GigerAttendance> {
        if (currentlySelectedStatus.isNullOrBlank() &&
            currentlySearchTerm.isNullOrBlank() &&
            currentlySelectedShiftTime.isNullOrBlank() &&
            currentlySelectedBusiness.isNullOrBlank()
        ) return currentlyShownAttendanceData ?: emptyList()

        return currentlyShownAttendanceData?.filter {
            if (!currentlySelectedStatus.isNullOrBlank()) currentlySelectedStatus == it.attendanceStatus else true
        }?.filter {
            if (currentlySearchTerm != null) {
                it.name?.contains(currentlySearchTerm!!, true) ?: false ||
                        it.phoneNumber?.contains(currentlySearchTerm!!, true) ?: false ||
                        it.role?.contains(currentlySearchTerm!!, true) ?: false
            } else
                true
        }?.filter {
            if (!currentlySelectedBusiness.isNullOrBlank()) currentlySelectedBusiness == it.companyName else true
        }?.filter {
            if (!currentlySelectedShiftTime.isNullOrBlank()) currentlySelectedShiftTime == it.shiftTime else true
        } ?: emptyList()
    }

    private fun prepareAllFilters() {
        val gigAttendanceData = currentlyShownAttendanceData ?: return
        val statuses = gigAttendanceData
            .filter {
                !it.attendanceStatus.isNullOrBlank()
            }.distinctBy {
                it.attendanceStatus
            }.map { gigerAttendanceItem ->
                AttendanceStatusAndCountItemData(
                    status = gigerAttendanceItem.attendanceStatus!!,
                    attendanceCount = gigAttendanceData.count { gigerAttendanceItem.attendanceStatus == it.attendanceStatus },
                    statusSelected = false
                )
            }.toMutableList()
            .apply {
                this.add(
                    0, AttendanceStatusAndCountItemData(
                        status = "All",
                        attendanceCount = gigAttendanceData.size,
                        statusSelected = true
                    )
                )
            }

        val businesses = gigAttendanceData
            .filter {
                !it.companyName.isNullOrBlank()
            }.map {
                it.companyName!!
            }.distinct()

        val slotTimings = gigAttendanceData
            .filter {
                !it.shiftTime.isNullOrBlank()
            }.map {
                it.shiftTime!!
            }.distinct()
            .map {
                AttendanceFilterItemShift(
                    shift = it,
                    shiftTimeForView = formatShiftTime(it)
                )
            }


        _filters.postValue(
            AttendanceFilters(
                shouldRemoveOlderStatusTabs = true,
                attendanceStatuses = statuses,
                business = businesses,
                shiftTimings = slotTimings
            )
        )
    }

    /**
     * Updates bussiness and slot filters, call on change of status
     */
    private fun updateBusinessAndSlotFilter() {
        val gigAttendanceData = currentlyShownAttendanceData ?: return

        if (!currentlySelectedStatus.isNullOrEmpty()) {
            error("updateBusinessAndSlotFilter called currentlySelectedStatus was blank or empty")
        }


        val gigWithCurrentStatus = gigAttendanceData
            .filter {
                currentlySelectedStatus == it.attendanceStatus
            }

        val businesses = gigWithCurrentStatus
            .filter {
                !it.companyName.isNullOrBlank()
            }.map {
                it.companyName!!
            }.distinct()

        val shiftTimings = gigWithCurrentStatus
            .filter {
                !it.shiftTime.isNullOrBlank()
            }.map {
                it.shiftTime!!
            }.distinct()
            .map {
                AttendanceFilterItemShift(
                    shift = it,
                    shiftTimeForView = formatShiftTime(it)
                )
            }

        _filters.postValue(
            AttendanceFilters(
                shouldRemoveOlderStatusTabs = false,
                attendanceStatuses = null,
                business = businesses,
                shiftTimings = shiftTimings
            )
        )
    }


    private fun updateSlotFilter() {
        val gigAttendanceData = currentlyShownAttendanceData ?: return

        val gigWithCurrentStatus = gigAttendanceData
            .filter {

                if (currentlySelectedStatus != null && currentlySelectedBusiness != null) {
                    it.gigStatus == currentlySelectedStatus && it.companyName == currentlySelectedBusiness
                } else if (currentlySelectedStatus != null) {
                    it.gigStatus == currentlySelectedStatus
                } else if (currentlySelectedBusiness != null) {
                    it.companyName == currentlySelectedBusiness
                } else {
                    true
                }
            }

        val shiftTimings = gigWithCurrentStatus
            .filter {
                !it.shiftTime.isNullOrBlank()
            }.map {
                it.shiftTime!!
            }.distinct()
            .map {
                AttendanceFilterItemShift(
                    shift = it,
                    shiftTimeForView = formatShiftTime(it)
                )
            }

        _filters.postValue(
            AttendanceFilters(
                shouldRemoveOlderStatusTabs = false,
                attendanceStatuses = null,
                business = null,
                shiftTimings = shiftTimings
            )
        )
    }

    private fun resetAllFilters() {
        currentlySelectedShiftTime = null
        currentlySelectedBusiness = null
        currentlySearchTerm = null
        currentlySelectedStatus = null
    }

    fun markUserCheckedIn(
        gigId: String,
        userName: String,
        gigerId: String,
        businessName: String
    ) = viewModelScope.launch {

        try {
            gigersAttendanceRepository.markUserAttendanceAsPresent(
                gigId
            )
            currentlyShownAttendanceData?.find {
                gigId == it.gigId
            }?.let {
                it.attendanceStatus = "Present"
                it.gigStatus = GigStatus.ONGOING.getStatusString()
                //event
                FirebaseAuth.getInstance().currentUser?.uid?.let {
                    val map = mapOf("Giger ID" to gigerId , "TL ID" to it, "Business Name" to businessName)
                    eventTracker.pushEvent(TrackingEventArgs("tl_marked_checkin",map))
                }
            }

            _markAttendanceState.postValue(
                GigerAttendanceUnderManagerViewModelMarkAttendanceState.UserMarkedPresent(
                    "$userName marked present"
                )
            )
            delay(300)
            updateStatusCounts()
            delay(300)
            filterCachedResultsAndEmit()
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

        currentlyShownAttendanceData?.find {
            gigId == it.gigId
        }?.let {
            it.attendanceStatus = "Declined"
            it.gigStatus = GigStatus.DECLINED.getStatusString()
        }
        updateStatusCounts()
        delay(300)
        filterCachedResultsAndEmit()
    }

    private fun updateStatusCounts() {

        val gigAttendanceData = currentlyShownAttendanceData ?: return
        val statuses = gigAttendanceData
            .filter {
                !it.attendanceStatus.isNullOrBlank()
            }.distinctBy {
                it.attendanceStatus
            }.map { gigerAttendanceItem ->
                AttendanceStatusAndCountItemData(
                    status = gigerAttendanceItem.attendanceStatus!!,
                    attendanceCount = gigAttendanceData.count { gigerAttendanceItem.attendanceStatus == it.attendanceStatus },
                    statusSelected = false
                )
            }.toMutableList()
            .apply {
                this.add(
                    0, AttendanceStatusAndCountItemData(
                        status = "All",
                        attendanceCount = gigAttendanceData.size,
                        statusSelected = true
                    )
                )
            }

        _filters.postValue(
            AttendanceFilters(
                shouldRemoveOlderStatusTabs = false,
                attendanceStatuses = statuses,
                business = null,
                shiftTimings = null
            )
        )

    }

    companion object {

        const val TAG = "GigerAttendanceUnderManagerViewModel"
    }

}
