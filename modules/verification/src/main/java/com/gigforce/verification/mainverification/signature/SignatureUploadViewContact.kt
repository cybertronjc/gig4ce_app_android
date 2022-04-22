package com.gigforce.verification.mainverification.signature

import android.net.Uri

sealed class SignatureViewEvents {

    data class SignatureCaptured(
        val signature: Uri
    ) : SignatureViewEvents()

    object DoneOrCancelButtonClicked : SignatureViewEvents()
}

sealed class SignatureUploadViewState {

    object CheckingExistingSignature : SignatureUploadViewState()

    data class ShowExistingExistingSignature(
        val signatureUri : Uri?
    ) : SignatureUploadViewState()


    data class ErrorWhileCheckingExsitingSignature(
        val error : String
    ) : SignatureUploadViewState()


    /**
     * ------------------------------
     * Removing Background signature
     * ------------------------------
     */

    object RemovingBackgroundFromSignature : SignatureUploadViewState()

    data class BackgroundRemovedFromSignature(
        val processedImage: Uri,
        val enableSubmitButton: Boolean
    ) : SignatureUploadViewState()

    data class ErrorWhileRemovingBackgroundFromSignature(
        val processedImage: Uri
    ) : SignatureUploadViewState()


    object UploadingSignature : SignatureUploadViewState()

    data class SignatureUploaded(
        val firebaseCompletePath: String,
        val firebaseImageFullUrl: String
    ) : SignatureUploadViewState()

    data class ErrorUploadingSignatureImage(
        val error: String
    ) : SignatureUploadViewState()

    object NavigateBackToPreviousScreen : SignatureUploadViewState()


}