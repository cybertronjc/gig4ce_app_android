package com.gigforce.lead_management.ui.reference_check

import com.gigforce.core.datamodels.profile.Reference

sealed class ReferenceCheckEvent {

    data class NameChanged(
        val name : String
    ) : ReferenceCheckEvent()

    data class RelationChanged(
        val relation : String
    ) : ReferenceCheckEvent()

    data class ContactNoChanged(
        val contactNo : String
    ) : ReferenceCheckEvent()

    data class SubmitButtonPressed(
        val userUid : String
    ) : ReferenceCheckEvent()
}

sealed class ReferenceCheckViewState {

    data class ValidationError(
        val nameValidationError: String? = null,
        val relationValidationError: String? = null,
        val contactValidationError: String? = null,
    ) : ReferenceCheckViewState()

    object SubmittingReferenceData : ReferenceCheckViewState()

    object FetchingReferenceDataFromProfile : ReferenceCheckViewState()

    data class PreviousReferenceDataFetched(
        val referenceData : Reference
    ) : ReferenceCheckViewState()

    data class ErrorWhileFetchingPreviousReferenceData(
        val error: String
    ) : ReferenceCheckViewState()

    object SubmittingReferenceDataSuccess : ReferenceCheckViewState()

    data class SubmittingReferenceDataError(
        val error: String,
        val shouldShowErrorButton: Boolean
    ) : ReferenceCheckViewState()
}