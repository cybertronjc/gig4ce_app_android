package com.gigforce.lead_management.ui.new_selection_form_2

import android.text.SpannedString
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import java.time.LocalDate

sealed class NewSelectionForm2ViewState  {

    object LoadingLocationAndTLData : NewSelectionForm2ViewState()

    data class LocationAndTlDataLoaded(
        val selectedCity : String?,
        val selectedReportingLocation : String?,
        val shiftAndTls : JoiningLocationTeamLeadersShifts,
        val locationType: String?,
        val doesUserHaveToUploadAnyVerificationDocuments : Boolean
    ) : NewSelectionForm2ViewState()

    data class ErrorWhileLoadingLocationAndTlData(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm2ViewState()

    data class OpenSelectCityScreen(
        val cities: List<ReportingLocationsItem>,
        val locationType: String?
    ) : NewSelectionForm2ViewState()

    data class OpenSelectOtherCityScreen(
        val cities: List<OtherCityClusterItem>,
        val locationType: String?
    ) : NewSelectionForm2ViewState()

    data class OpenSelectClusterScreen(
        val cities: List<OtherCityClusterItem>,
        val locationType: String?
    ) : NewSelectionForm2ViewState()

    data class OpenInputSalaryScreen(
        val cities: List<OtherCityClusterItem>,
        val locationType: String?
    ) : NewSelectionForm2ViewState()

    data class OpenSelectReportingScreen(
        var selectedCity : ReportingLocationsItem,
        val reportingLocations: List<ReportingLocationsItem>
    ) : NewSelectionForm2ViewState()

    data class OpenSelectClientTlScreen(
        val tls: List<BusinessTeamLeadersItem>
    ) : NewSelectionForm2ViewState()

    data class ValidationError(
        val cityError: SpannedString? = null,
        val reportingLocationError: SpannedString? = null,
        val clientTLError: SpannedString? = null,
        val assignGigsFromError: SpannedString? = null,
        val shiftsError: SpannedString? = null,
        val secondaryPhoneNumberError: SpannedString? = null
    ) : NewSelectionForm2ViewState()


    object SubmittingJoiningData : NewSelectionForm2ViewState()

    data class JoiningDataSubmitted(
        val shareLink : String,
        val businessName: String,
        val jobProfileName: String
    ) : NewSelectionForm2ViewState()

    data class NavigateToJoiningVerificationForm(
        val joiningRequest : SubmitJoiningRequest,
        val userId : String,
        val verificationDynamicFields : List<DynamicVerificationField>
    ) : NewSelectionForm2ViewState()

    data class ErrorWhileSubmittingJoiningData(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm2ViewState()

    data class EnteredPhoneNumberSanitized(
        val sanitizedPhoneNumber : String
    ): NewSelectionForm2ViewState()
}

sealed class NewSelectionForm2Events {

    data class JoiningDataReceivedFromPreviousScreen(
        val submitJoiningRequest: SubmitJoiningRequest,
        val verificationRelatedDynamicInputsFields: ArrayList<DynamicVerificationField>
    ) :NewSelectionForm2Events()

    object SelectCityClicked : NewSelectionForm2Events()

    object SelectOtherCityClicked : NewSelectionForm2Events()


    object SelectReportingLocationClicked : NewSelectionForm2Events()

    object SelectClientTLClicked : NewSelectionForm2Events()

    data class SecondaryPhoneNumberChanged(
        val secondaryPhoneNumber : String
    ) :NewSelectionForm2Events()

    data class CitySelected(
        val city: ReportingLocationsItem
    ) : NewSelectionForm2Events()

    data class ReportingLocationSelected(
        val citySelected: ReportingLocationsItem,
        val reportingLocation: ReportingLocationsItem
    ) : NewSelectionForm2Events()

    data class ClientTLSelected(
        val teamLeader: BusinessTeamLeadersItem
    ) : NewSelectionForm2Events()

    data class DateOfJoiningSelected(
        val date: LocalDate
    ) : NewSelectionForm2Events()

    data class ShiftSelected(
        val shifts: List<ShiftTimingItem>
    ) : NewSelectionForm2Events()

    data class SubmitButtonPressed(
        val dataFromDynamicFields : MutableList<DataFromDynamicInputField>
    ) : NewSelectionForm2Events()
}