package com.gigforce.lead_management.ui.share_application_link

sealed class ShareReferralViewState {

    object SharingAndUpdatingJoiningDocument : ShareReferralViewState()

    data class ErrorInCreatingOrUpdatingDocument(
        val error: String
    ) : ShareReferralViewState()

    data class UnableToCreateShareLink(
        val error: String
    ) : ShareReferralViewState()

    data class OpenWhatsAppToShareDocumentSharingDocument(
        val shareType : String,
        val shareLink : String
    ) : ShareReferralViewState()

    data class OpenOtherAppsToShareDocumentSharingDocument(
        val shareType : String,
        val shareLink : String
    ) : ShareReferralViewState()

    object DocumentUpdatedAndReferralShared : ShareReferralViewState()
}