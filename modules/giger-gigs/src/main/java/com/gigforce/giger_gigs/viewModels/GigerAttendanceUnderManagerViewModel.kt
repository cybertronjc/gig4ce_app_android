package com.gigforce.giger_gigs.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.gig.GigerAttendance
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.giger_gigs.models.AttendanceRecyclerItemData
import com.gigforce.giger_gigs.models.AttendanceStatusAndCountItemData
import com.gigforce.giger_gigs.repositories.GigersAttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

sealed class GigerAttendanceUnderManagerViewModelState {

    object LoadingDataFromServer : GigerAttendanceUnderManagerViewModelState()

    object NoAttendanceFound : GigerAttendanceUnderManagerViewModelState()

    data class ErrorInLoadingDataFromServer(
            val error: String,
            val shouldShowErrorButton: Boolean
    ) : GigerAttendanceUnderManagerViewModelState()

    data class AttendanceDataLoaded(
            val attendanceStatuses: List<AttendanceStatusAndCountItemData>,
            val attendanceItemData: List<AttendanceRecyclerItemData>
    ) : GigerAttendanceUnderManagerViewModelState()
}

@HiltViewModel
class GigerAttendanceUnderManagerViewModel @Inject constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {

    //todo shift up
    private val gigersAttendanceRepository: GigersAttendanceRepository = GigersAttendanceRepository(buildConfig)

    /* data*/
    private var currentlyShownAttendanceData: List<GigerAttendance>? = null

    //Filters
    private var currentlyFetchingForDate: LocalDate = LocalDate.now()
    private var currentlySelectedStatus: String? = null
    private var currentlySearchTerm: String? = null

    //To view Observables
    private val _gigerAttendanceUnderManagerViewState =
            MutableLiveData<GigerAttendanceUnderManagerViewModelState>()
    val gigerAttendanceUnderManagerViewState: LiveData<GigerAttendanceUnderManagerViewModelState> =
            _gigerAttendanceUnderManagerViewState


    fun fetchUsersAttendanceDate(
            date: LocalDate
    ) = viewModelScope.launch(Dispatchers.IO) {
        currentlyFetchingForDate = date

        _gigerAttendanceUnderManagerViewState.postValue(
                GigerAttendanceUnderManagerViewModelState.LoadingDataFromServer
        )

        try {
            delay(200)
            val gigersAttendance = gigersAttendanceRepository.getAttendance(
                    date
            )
            currentlyShownAttendanceData = gigersAttendance

            if (gigersAttendance.isEmpty()) {

                _gigerAttendanceUnderManagerViewState.postValue(
                        GigerAttendanceUnderManagerViewModelState.NoAttendanceFound
                )
            } else {
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
    ): Pair<List<AttendanceStatusAndCountItemData>,
            List<AttendanceRecyclerItemData>> {

        val statusesAndTheirCounts = prepareAttendanceStatusMasterAndTheirCount(
                gigersAttendance
        )
        val attendanceGroupedByShiftAndCompany = groupAttendanceByShiftTimeAndCompany(
                gigersAttendance
        )
        val attendanceRecyclerItemData: MutableList<AttendanceRecyclerItemData> =
                mapGroupedAttendanceForRecyclerView(
                        attendanceGroupedByShiftAndCompany
                )

        return statusesAndTheirCounts to attendanceRecyclerItemData
    }

    private fun mapGroupedAttendanceForRecyclerView(
            attendanceGroupedByShiftAndCompany: List<Map<String, Map<String, List<GigerAttendance>>>>
    ): MutableList<AttendanceRecyclerItemData> {
        val attendanceRecyclerItemData: MutableList<AttendanceRecyclerItemData> = mutableListOf()
        attendanceGroupedByShiftAndCompany.forEach {

            it.forEach { (shiftTime, companyToAttendanceGroup) ->
                attendanceRecyclerItemData.add(
                        AttendanceRecyclerItemData.AttendanceRecyclerItemShiftNameData(
                                shiftTime
                        )
                )

                companyToAttendanceGroup.forEach { (companyName, gigerAttendance) ->
                    attendanceRecyclerItemData.add(
                            AttendanceRecyclerItemData.AttendanceRecyclerItemBusinessData(
                                    companyName
                            )
                    )

                    gigerAttendance.forEach {
                        attendanceRecyclerItemData.add(
                                mapRemoteGigerAttendanceToRecyclerViewAttendance(it)
                        )
                    }
                }
            }
        }

        return attendanceRecyclerItemData
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
            gigerAttendance: GigerAttendance
    ): AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData {

        return AttendanceRecyclerItemData.AttendanceRecyclerItemAttendanceData(
                attendanceStatus = gigerAttendance.attendanceStatus!!,
                gigId = gigerAttendance.gigId ?: "",
                gigerId = gigerAttendance.uid!!,
                gigerName = gigerAttendance.name ?: "Name: NA",
                gigerPhoneNumber = gigerAttendance.phoneNumber ?: "",
                gigerDesignation = gigerAttendance.role ?: "Role: NA",
                gigerImage = gigerAttendance.profilePicture ?: ""
        )
    }

    fun filterAttendanceByStatus(
            status: String?
    ) = viewModelScope.launch {
        currentlySelectedStatus = status
        filterCachedResultsAndEmit()
    }

    fun searchAttendance(
            searchTerm: String
    ) = viewModelScope.launch {
        currentlySearchTerm = searchTerm
        filterCachedResultsAndEmit()
    }

    private fun filterCachedResultsAndEmit() {
        if(currentlyShownAttendanceData == null){
            return
        }

        if (currentlyShownAttendanceData!!.isEmpty()) {
            _gigerAttendanceUnderManagerViewState.postValue(
                    GigerAttendanceUnderManagerViewModelState.NoAttendanceFound
            )
            return
        }


        val filteredAttendanceData = filterAttendanceData()
        val statusAndAttendance = prepareStatusAndAttendanceItems(
                filteredAttendanceData
        )

        _gigerAttendanceUnderManagerViewState.postValue(
                GigerAttendanceUnderManagerViewModelState.AttendanceDataLoaded(
                        attendanceStatuses = statusAndAttendance.first,
                        attendanceItemData = statusAndAttendance.second
                )
        )
    }

    private fun filterAttendanceData(): List<GigerAttendance> {
        if (currentlySelectedStatus.isNullOrBlank() && currentlySearchTerm.isNullOrBlank())
            return currentlyShownAttendanceData ?: emptyList()

        return currentlyShownAttendanceData?.filter {
            if (!currentlySelectedStatus.isNullOrBlank()) currentlySelectedStatus == it.attendanceStatus else true
        }?.filter {
            if (currentlySearchTerm != null) {
                it.name?.contains(currentlySearchTerm!!,true) ?: false ||
                it.phoneNumber?.contains(currentlySearchTerm!!,true) ?: false ||
                it.role?.contains(currentlySearchTerm!!,true) ?: false
            } else
                true
        } ?: emptyList()
    }


    companion object {

        const val TAG = "GigerAttendanceUnderManagerViewModel"
    }

}
