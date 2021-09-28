package com.gigforce.lead_management.ui.new_selection_form_2

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@HiltViewModel
class NewSelectionForm2ViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        private const val TAG = "NewSelectionForm1ViewModel"
    }

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD

    //State Observables
    private val _viewState = MutableLiveData<NewSelectionForm2ViewState?>()
    val viewState: LiveData<NewSelectionForm2ViewState?> = _viewState

    //Data
    private var selectedDateOfJoining: LocalDate = LocalDate.now()
    private var selectedCity: ReportingLocationsItem? = null
    private var selectedReportingLocation: ReportingLocationsItem? = null
    private var selectedTL: BusinessTeamLeadersItem? = null
    private var selectedShifts: MutableList<ShiftTimingItem> = mutableListOf()
    private lateinit var joiningLocationsAndTLs: JoiningLocationTeamLeadersShifts
    private lateinit var joiningRequest: SubmitJoiningRequest


    fun handleEvent(
        event: NewSelectionForm2Events
    ) {
        when (event) {
            is NewSelectionForm2Events.DateOfJoiningSelected -> {
                selectedDateOfJoining = event.date
            }
            NewSelectionForm2Events.SelectCityClicked -> openSelectCityScreen()
            NewSelectionForm2Events.SelectReportingLocationClicked -> openSelectReportingLocationsScreen()
            NewSelectionForm2Events.SelectClientTLClicked -> openSelectBusinessTLScreen()
            is NewSelectionForm2Events.ShiftSelected -> {
                selectedShifts = event.shifts.toMutableList()
            }
            is NewSelectionForm2Events.CitySelected -> {
                selectedCity = event.city
                selectedReportingLocation = null
            }
            is NewSelectionForm2Events.ClientTLSelected -> {
                selectedTL = event.teamLeader
            }
            is NewSelectionForm2Events.ReportingLocationSelected -> {
                selectedCity = event.citySelected
                selectedReportingLocation = event.reportingLocation
            }
            is NewSelectionForm2Events.SubmitButtonPressed -> {
                selectedShifts = event.shiftSelected.toMutableList()
                validateDataAndSubmit()
            }
            is NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen -> {
                joiningRequest = event.submitJoiningRequest
                fetchJoiningForm2Data(joiningRequest.business.id!!)
            }
        }
    }

    private fun openSelectCityScreen() {
        val cities = joiningLocationsAndTLs.reportingLocations.filter {
            it.type == "city"
        }

        cities.onEach {
            it.selected = it.id == selectedCity?.id
            it.reportingLocations = getReportingLocations(it.id)
        }

        _viewState.value = NewSelectionForm2ViewState.OpenSelectCityScreen(
            cities = cities
        )
        _viewState.value = null
    }

    private fun getReportingLocations(
        cityId: String?
    ): List<ReportingLocationsItem> {
        val cityIdNonNull = cityId ?: return emptyList()

        val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
            it.cityId == cityIdNonNull && it.type == "office"
        }

        reportingLocations.onEach {
            it.selected = it.id == cityIdNonNull
        }

        return reportingLocations
    }


    private fun openSelectReportingLocationsScreen() {
        if (selectedCity == null) {
            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = "Select City first"
            )
            _viewState.value = null
        } else {
            val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
                it.cityId == selectedCity?.cityId && it.type == "office"
            }

            reportingLocations.onEach {
                it.selected = it.id == selectedReportingLocation?.id

            }

            _viewState.value = NewSelectionForm2ViewState.OpenSelectReportingScreen(
                selectedCity!!,
                reportingLocations
            )
            _viewState.value = null
        }
    }

    private fun openSelectBusinessTLScreen() {

        joiningLocationsAndTLs.businessTeamLeaders.onEach {
            it.selected = it.id == selectedTL?.id
        }
        _viewState.value = NewSelectionForm2ViewState.OpenSelectClientTlScreen(
            joiningLocationsAndTLs.businessTeamLeaders
        )
        _viewState.value = null
    }

    fun fetchJoiningForm2Data(
        businessId: String
    ) = viewModelScope.launch {

        if (::joiningLocationsAndTLs.isInitialized) {

            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value = NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                selectedCity?.name,
                selectedReportingLocation?.name,
                joiningLocationsAndTLs
            )
            return@launch
        }

        _viewState.value = NewSelectionForm2ViewState.LoadingLocationAndTLData
        logger.d(
            TAG,
            "fetching location and tls..."
        )

        try {
            val locationAndTlsData = leadManagementRepository.getBusinessLocationsAndTeamLeaders(
                businessId = businessId
            )
            joiningLocationsAndTLs = locationAndTlsData

            logger.d(
                TAG,
                "location and tl data received from server"
            )
            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value =
                NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                    selectedCity?.name,
                    selectedReportingLocation?.name,
                    locationAndTlsData
                )
        } catch (e: Exception) {
            logger.e(
                TAG,
                "while loading joining form2 data",
                e
            )

            _viewState.value = NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData(
                error = e.message ?: "Unable to load locations and tl data",
                shouldShowErrorButton = false
            )
        }
    }

    private fun checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne() {
        if (selectedCity == null) {

            val cities = joiningLocationsAndTLs.reportingLocations.filter {
                it.type == "city"
            }
            if (cities.size == 1) {
                cities.onEach {
                    it.selected = it.id == selectedCity?.id
                    it.reportingLocations = getReportingLocations(it.id)
                }
                selectedCity = cities.first()

                if (selectedReportingLocation == null) {
                    val reportingLocations = joiningLocationsAndTLs.reportingLocations.filter {
                        it.cityId == selectedCity?.cityId && it.type == "office"
                    }

                    if (reportingLocations.size == 1) {
                        reportingLocations.onEach {
                            it.selected = it.id == selectedReportingLocation?.id
                        }
                        selectedReportingLocation = reportingLocations.first()
                    }
                }
            }
        }


    }

    private fun validateDataAndSubmit() {

        if (selectedCity == null) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = "Please select city"
            )
            return
        }
        joiningRequest.city = selectedCity!!

        if (selectedCity!!.reportingLocations.isNotEmpty() && selectedReportingLocation == null) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                reportingLocationError = "Please select reporting location"
            )
            return
        }
        joiningRequest.reportingLocation = selectedReportingLocation
        joiningRequest.assignGigsFrom = dateFormatter.format(selectedDateOfJoining)

        if (selectedShifts.isEmpty()) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                shiftsError = "Please select at least one shift"
            )
            return
        }
        joiningRequest.shifts = selectedShifts


        submitJoiningData(joiningRequest)
    }

    private fun submitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) = viewModelScope.launch {
        logger.d(
            TAG,
            "Assigning gigs....",
        )

        try {
            _viewState.value = NewSelectionForm2ViewState.SubmittingJoiningData


            logger.d(
                TAG,
                "Assigning gigs [Data]...., $joiningRequest",
            )

            val shareLink = try {
                leadManagementRepository.createJobProfileReferralLink(joiningRequest.jobProfile.id!!)
            } catch (e: Exception) {
                logger.d(TAG, "error while creating job profile share link", e)
                ""
            }

            joiningRequest.shareLink
            leadManagementRepository.submitJoiningRequest(
                joiningRequest
            )

            _viewState.value = NewSelectionForm2ViewState.JoiningDataSubmitted(
                shareLink = shareLink
            )
            _viewState.value = null
            logger.d(
                TAG,
                "[Success] Gigs assigned"
            )
        } catch (e: Exception) {
            _viewState.value = NewSelectionForm2ViewState.ErrorWhileSubmittingJoiningData(
                error = e.message ?: "Unable to submit joining request, please try again later",
                shouldShowErrorButton = false
            )
            _viewState.value = null

            logger.e(
                TAG,
                "[Failure] Gigs assign failed",
                e
            )
        }

    }
}