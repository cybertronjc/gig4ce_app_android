package com.gigforce.lead_management.ui.new_selection_form

import android.text.SpannedString
import com.gigforce.common_ui.dynamic_fields.data.DataFromDynamicInputField
import com.gigforce.common_ui.dynamic_fields.data.DynamicField
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.datamodels.profile.ProfileData

sealed class NewSelectionForm1ViewState {

    object LoadingBusinessAndJobProfiles : NewSelectionForm1ViewState()

    data class JobProfilesAndBusinessLoadSuccess(
        val selectedTeamLeader : TeamLeader?
    ) : NewSelectionForm1ViewState()

    data class ErrorWhileLoadingBusinessAndJobProfiles(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm1ViewState()

    data class OpenSelectedBusinessScreen(
        val business: List<JoiningBusinessAndJobProfilesItem>
    ) : NewSelectionForm1ViewState()

    data class OpenSelectedJobProfileScreen(
        val selectedBusiness: JoiningBusinessAndJobProfilesItem,
        val jobProfiles: List<JobProfilesItem>
    ) : NewSelectionForm1ViewState()

    data class OpenSelectTLScreen(
        val teamLeaders: List<TeamLeader>
    ) : NewSelectionForm1ViewState()

    object CheckingForUserDetailsFromProfiles : NewSelectionForm1ViewState()

    data class ValidationError(
        val invalidMobileNoMessage: SpannedString? = null,
        val gigerNameError: SpannedString? = null,
        val businessError: SpannedString? = null,
        val jobProfilesError: SpannedString? = null,
        val reportingTLError: SpannedString? = null,
    ) : NewSelectionForm1ViewState()

    data class UserDetailsFromProfiles(
        val profile: ProfileData
    ) : NewSelectionForm1ViewState()

    data class ErrorWhileCheckingForUserInProfile(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm1ViewState()


    data class NavigateToForm2(
        val submitJoiningRequest: SubmitJoiningRequest,
        val dynamicInputsFields : List<DynamicField>,
        val verificationRelatedDynamicInputsFields : List<DynamicVerificationField>
    ) : NewSelectionForm1ViewState()

    object EnableSubmitButton : NewSelectionForm1ViewState()

    object DisableSubmitButton : NewSelectionForm1ViewState()

    data class ShowJobProfileRelatedField(
        val dynamicFields : List<DynamicField>
    ): NewSelectionForm1ViewState()

    data class EnteredPhoneNumberSanitized(
        val sanitizedPhoneNumber : String
    ): NewSelectionForm1ViewState()
}

sealed class NewSelectionForm1Events {

    data class ContactNoChanged(
        val mobileNo: String
    ) : NewSelectionForm1Events()

    data class GigerNameChanged(
        val name: String
    ) : NewSelectionForm1Events()

    data class BusinessSelected(
        val business: JoiningBusinessAndJobProfilesItem
    ) : NewSelectionForm1Events()

    data class JobProfileSelected(
        val jobProfile: JobProfilesItem
    ) : NewSelectionForm1Events()

    data class ReportingTeamLeaderSelected(
        val teamLeader: TeamLeader
    ) : NewSelectionForm1Events()

    data class SubmitButtonPressed(
        val dataFromDynamicFields : MutableList<DataFromDynamicInputField>
    ) : NewSelectionForm1Events()

    object OpenSelectBusinessScreenSelected : NewSelectionForm1Events()

    object OpenSelectJobProfileScreenSelected : NewSelectionForm1Events()
}