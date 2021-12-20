package com.gigforce.lead_management.ui.new_selection_form_3_verification_documents

import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.viewdatamodels.leadManagement.SubmitJoiningRequest
import com.gigforce.common_ui.viewmodels.verification.SharedVerificationViewModelEvent
import com.gigforce.lead_management.models.WhatsappTemplateModel

sealed class NewSelectionForm3ViewState {

    object CheckingVerificationDocumentsStatus : NewSelectionForm3ViewState()

    data class ShowVerificationDocumentFields(
        val requiredVerificationDocument : List<DynamicVerificationField>
    ) : NewSelectionForm3ViewState()

    data class ErrorWhileCheckingVerificationStatus(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm3ViewState()


    object SubmittingJoiningData : NewSelectionForm3ViewState()

    data class JoiningDataSubmitted(
        val whatsappTemplate : WhatsappTemplateModel
    ) : NewSelectionForm3ViewState()

    data class ErrorWhileSubmittingJoiningData(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : NewSelectionForm3ViewState()
}

sealed class NewSelectionForm3UiEffects {

    object EnableSubmitButton : NewSelectionForm3UiEffects()

    object DisableSubmitButton : NewSelectionForm3UiEffects()

}

sealed class NewSelectionForm3Events {

    data class RequiredVerificationDocumentListAcquiredFromPreviousPage(
        val joiningRequest : SubmitJoiningRequest,
        val userUid : String,
        val requiredVerificationDocument : List<DynamicVerificationField>
    ) : NewSelectionForm3Events()

    object SubmitButtonPressed : NewSelectionForm3Events()
}