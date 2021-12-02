package com.gigforce.lead_management.ui.new_selection_form_2

import android.content.Context
import androidx.core.text.bold
import androidx.core.text.buildSpannedString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.ValidationHelper
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.lead_management.R
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1ViewModel
import com.gigforce.lead_management.ui.new_selection_form.NewSelectionForm1ViewState
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
    private val profileFirebaseRepository: ProfileFirebaseRepository,
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
    private var secondaryPhoneNumber : String? = null

    //Data from previous screen
    private lateinit var joiningLocationsAndTLs: JoiningLocationTeamLeadersShifts
    private lateinit var joiningRequest: SubmitJoiningRequest
    private var currentlySubmittingAnyJoiningRequuest = false
    private var dataFromDynamicFieldsFromPreviousPages : List<DataFromDynamicInputField> = emptyList()

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
                validateDataAndSubmit(
                    event.dataFromDynamicFields
                )
            }
            is NewSelectionForm2Events.JoiningDataReceivedFromPreviousScreen -> {
                joiningRequest = event.submitJoiningRequest
                dataFromDynamicFieldsFromPreviousPages = event.submitJoiningRequest.dataFromDynamicFields
                joiningRequest.dataFromDynamicFields = emptyList()


                fetchJoiningForm2Data(
                    joiningRequest.business.id!!,
                    joiningRequest.jobProfile.id!!,
                )
            }
            is NewSelectionForm2Events.SecondaryPhoneNumberChanged -> secondaryMobileNumberChanged(event.secondaryPhoneNumber)
        }
    }

    private fun secondaryMobileNumberChanged(
        mobileNumber: String
    ) = viewModelScope.launch {

        if( secondaryPhoneNumber == mobileNumber){
            return@launch
        }
        secondaryPhoneNumber = mobileNumber

        if(mobileNumber.length == 3) { //+91

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = null
            )
            _viewState.value = null
            return@launch
        }

        val doesMobileNoContainsMoreThan10digits = mobileNumber.length > 13
        if (doesMobileNoContainsMoreThan10digits) {
            logger.d(TAG,"alternate Mobile no received : '${mobileNumber}', checking if can be fixed..")
            tryToFixPhoneNumberEntered(mobileNumber)
            return@launch
        }

        val mobileNumber10Dig = mobileNumber.substring(3)
        val mobileNumberValid = ValidationHelper.isValidIndianMobileNo(mobileNumber10Dig)

        if(!mobileNumberValid) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.number_you_entered_is_invalid_number_lead)
                    )
                }
            )
            _viewState.value = null
        } else{

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                secondaryPhoneNumberError = null
            )
            _viewState.value = null
        }
    }

    private fun tryToFixPhoneNumberEntered(
        mobileNo: String
    ) {
        if (mobileNo.length <= 13) {
            //Not fixing mobile number that have less than 11 (+91 + phone number)Digits
            return
        }

        //replacing 91 , starting 0s and spaces from text with empty string
        val sanitizedNumber = mobileNo.substring(3).replace(
            "(\\\\s|^0+|^91)".toRegex(), ""
        )

        logger.d(TAG,"phone number sanitized : $sanitizedNumber")

        if (ValidationHelper.isValidIndianMobileNo(sanitizedNumber)) {
            _viewState.value = NewSelectionForm2ViewState.EnteredPhoneNumberSanitized(sanitizedNumber)
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
            cities = cities.sortedBy {
                it.name
            },
            joiningRequest.jobProfile.locationType
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
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.select_city_to_select_reporting_location_lead)
                    )
                }
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
                reportingLocations.sortedBy {
                    it.name
                }
            )
            _viewState.value = null
        }
    }

    private fun openSelectBusinessTLScreen() {

        joiningLocationsAndTLs.businessTeamLeaders.onEach {
            it.selected = it.id == selectedTL?.id
        }
        _viewState.value = NewSelectionForm2ViewState.OpenSelectClientTlScreen(
            joiningLocationsAndTLs.businessTeamLeaders.sortedBy {
                it.name
            }
        )
        _viewState.value = null
    }

    fun fetchJoiningForm2Data(
        businessId: String,
        jobProfileId: String
    ) = viewModelScope.launch {

        if (::joiningLocationsAndTLs.isInitialized) {

            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value = NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                selectedCity?.name,
                selectedReportingLocation?.name,
                joiningLocationsAndTLs,
                joiningRequest.jobProfile.locationType
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
                businessId = businessId,
                jobProfileId = jobProfileId
            )
            joiningLocationsAndTLs = locationAndTlsData
            if (locationAndTlsData.shiftTiming.isEmpty()) {
                _viewState.value = NewSelectionForm2ViewState.ErrorWhileLoadingLocationAndTlData(
                    error = "Cannot make joining, Please contact your manager",
                    shouldShowErrorButton = false
                )
                return@launch
            }

            logger.d(
                TAG,
                "location and tl data received from server"
            )
            checkIfCityAndReportingLocationIsSelectedElseSelectedFirstOne()

            _viewState.value =
                NewSelectionForm2ViewState.LocationAndTlDataLoaded(
                    selectedCity?.name,
                    selectedReportingLocation?.name,
                    locationAndTlsData,
                    joiningRequest.jobProfile.locationType
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

    private fun validateDataAndSubmit(
        dataFromDynamicFields: MutableList<DataFromDynamicInputField>
    ) {

        if (selectedCity == null) {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                cityError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.please_select_city_lead)
                    )
                }
            )
            return
        }
        joiningRequest.city = selectedCity!!

        if (selectedCity!!.reportingLocations.isNotEmpty() && selectedReportingLocation == null && joiningRequest.jobProfile.locationType == "On Site") {

            _viewState.value = NewSelectionForm2ViewState.ValidationError(
                reportingLocationError = buildSpannedString {
                    bold {
                        append(appContext.getString(R.string.note_with_colon_lead))
                    }
                    append(
                        appContext.getString(R.string.please_select_reporting_location_lead)
                    )
                }
            )
            return
        }
        if (joiningRequest.jobProfile.locationType == "On Site"){
            joiningRequest.reportingLocation = selectedReportingLocation
        }else{
            joiningRequest.reportingLocation = null
        }

        if(!secondaryPhoneNumber.isNullOrBlank() && secondaryPhoneNumber!!.length > 3) { //+91

            val mobileNumber10Dig = secondaryPhoneNumber!!.substring(3)
            val mobileNumberValid = ValidationHelper.isValidIndianMobileNo(mobileNumber10Dig)

            if(!mobileNumberValid) {

                _viewState.value = NewSelectionForm2ViewState.ValidationError(
                    secondaryPhoneNumberError = buildSpannedString {
                        bold {
                            append(appContext.getString(R.string.note_with_colon_lead))
                        }
                        append(
                            appContext.getString(R.string.number_you_entered_is_invalid_number_lead)
                        )
                    }
                )
                _viewState.value = null
                return
            }

            joiningRequest.secondaryMobileNumber = secondaryPhoneNumber
        } else{
            joiningRequest.secondaryMobileNumber = null
        }

        joiningRequest.assignGigsFrom = dateFormatter.format(selectedDateOfJoining)

        val selectedShift = joiningLocationsAndTLs.shiftTiming.firstOrNull()
        if (selectedShift != null)
            joiningRequest.shifts = listOf(selectedShift)

        //Cleaning up Final JSON
        joiningRequest.business.jobProfiles = emptyList()
        joiningRequest.jobProfile.dynamicFields = emptyList()

        joiningRequest.dataFromDynamicFields = dataFromDynamicFieldsFromPreviousPages + dataFromDynamicFields
        submitJoiningData(
            joiningRequest
        )
    }


    private fun submitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) = viewModelScope.launch {

        if (currentlySubmittingAnyJoiningRequuest) {
            logger.d(
                TAG,
                "Already a joining request submission in progress, ignoring this one",
            )
            return@launch
        }

        currentlySubmittingAnyJoiningRequuest = true
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

            joiningRequest.shareLink = shareLink
            leadManagementRepository.submitJoiningRequest(
                joiningRequest
            )

            _viewState.value = NewSelectionForm2ViewState.JoiningDataSubmitted(
                shareLink = shareLink,
                businessName = joiningRequest.business.name.toString(),
                jobProfileName = joiningRequest.jobProfile.name.toString()
            )
            _viewState.value = null
            logger.d(
                TAG,
                "[Success] Gigs assigned"
            )
            currentlySubmittingAnyJoiningRequuest = false
        } catch (e: Exception) {

            currentlySubmittingAnyJoiningRequuest = false
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