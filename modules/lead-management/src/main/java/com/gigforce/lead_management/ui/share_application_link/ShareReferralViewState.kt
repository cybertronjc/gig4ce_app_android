package com.gigforce.lead_management.ui.share_application_link

sealed class ShareReferralViewState {

    object SharingAndUpdatingJoiningDocument : ShareReferralViewState()

    data class ErrorInCreatingOrUpdatingDocument(
        val error: String
    ) : ShareReferralViewState()

    data class DocumentUpdatesButErrorInSharingDocument(
        val error: String
    ) : ShareReferralViewState()

    object DocumentUpdatedAndReferralShared : ShareReferralViewState()
}