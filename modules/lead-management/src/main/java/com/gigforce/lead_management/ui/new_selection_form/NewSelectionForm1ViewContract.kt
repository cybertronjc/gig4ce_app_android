package com.gigforce.lead_management.ui.new_selection_form

import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusinessAndJobProfilesItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.SubmitJoiningRequest
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.lead_management.models.JoiningStatusAndCountItemData

sealed class NewSelectionForm1ViewState {

    object LoadingBusinessAndJobProfiles : NewSelectionForm1ViewState()

    object JobProfilesAndBusinessLoadSuccess : NewSelectionForm1ViewState()

    data class ErrorWhileLoadingBusinessAndJobProfiles(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm1ViewState()

    data class OpenSelectedBusinessScreen(
        val business: List<JoiningBusinessAndJobProfilesItem>
    ) : NewSelectionForm1ViewState()

    data class OpenSelectedJobProfileScreen(
        val jobProfiles: List<JobProfilesItem>
    ) : NewSelectionForm1ViewState()

    object CheckingForUserDetailsFromProfiles : NewSelectionForm1ViewState()

    data class ValidationError(
        val invalidMobileNoMessage: String? = null,
        val gigerNameError: String? = null,
        val gigerClientIdError: String? = null,
        val businessError: String? = null,
        val jobProfilesError: String? = null,
    ) : NewSelectionForm1ViewState()

    data class UserDetailsFromProfiles(
        val profile: ProfileData
    ) : NewSelectionForm1ViewState()

    data class ErrorWhileCheckingForUserInProfile(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm1ViewState()


    data class NavigateToForm2(
        val submitJoiningRequest: SubmitJoiningRequest
    ) : NewSelectionForm1ViewState()
}

sealed class NewSelectionForm1Events {

    data class ContactNoChanged(
        val mobileNo: String
    ) : NewSelectionForm1Events()

    data class GigerNameChanged(
        val name: String
    ) : NewSelectionForm1Events()

    data class GigerClientIdChanged(
        val clientId: String
    ) : NewSelectionForm1Events()

    data class BusinessSelected(
        val business: JoiningBusinessAndJobProfilesItem
    ) : NewSelectionForm1Events()

    data class JobProfileSelected(
        val jobProfile: JobProfilesItem
    ) : NewSelectionForm1Events()

    object SubmitButtonPressed : NewSelectionForm1Events()

    object OpenSelectBusinessScreenSelected : NewSelectionForm1Events()

    object OpenSelectJobProfileScreenSelected : NewSelectionForm1Events()
}

data class JoiningFilters(
    val shouldRemoveOlderStatusTabs: Boolean,
    val attendanceStatuses: List<JoiningStatusAndCountItemData>?
)