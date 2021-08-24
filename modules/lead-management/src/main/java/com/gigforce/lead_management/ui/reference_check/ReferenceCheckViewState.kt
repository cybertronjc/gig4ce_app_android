package com.gigforce.lead_management.ui.reference_check

sealed class ReferenceCheckViewState {

    data class ValidationError(
        val nameValidationError: String? = null,
        val relationValidationError: String? = null,
        val contactValidationError: String? = null,
    ) : ReferenceCheckViewState()

    object SubmittingReferenceData : ReferenceCheckViewState()

    object SubmittingReferenceDataSuccess : ReferenceCheckViewState()

    data class SubmittingReferenceDataError(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : ReferenceCheckViewState()
}