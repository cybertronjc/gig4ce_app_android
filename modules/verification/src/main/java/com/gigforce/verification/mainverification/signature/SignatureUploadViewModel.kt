package com.gigforce.verification.mainverification.signature

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.configrepository.SignatureRepository
import com.gigforce.common_ui.repository.GigerVerificationRepository
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject



@HiltViewModel
class SignatureUploadViewModel @Inject constructor(
    private val signatureRepository: SignatureRepository,
    private val verificationRepository: GigerVerificationRepository,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {

        const val TAG = "SignatureUploadViewModel"
    }

    private val _viewState = MutableLiveData<SignatureUploadViewState>()
    val viewState: LiveData<SignatureUploadViewState> = _viewState

    private var processedImageUri: Uri? = null
    private var backgroundRemoved = false


    override fun onCleared() {
        super.onCleared()
        backgroundRemoved = false
    }

    var userId: String = ""
        get() = if (field.isEmpty()) {
            firebaseAuthStateListener.getCurrentSignInUserInfoOrThrow().uid
        } else {
            field
        }

    var shouldRemoveBackgroundFromSignature: Boolean = false

    fun checkForExistingSignature() = viewModelScope.launch {

        _viewState.value = SignatureUploadViewState.CheckingExistingSignature

        try {

            val verificationDocRef = verificationRepository.verificationDocumentReference(userId)
                .getOrThrow()

            if (verificationDocRef.exists()) {

                val verificationData = verificationDocRef.toObject(VerificationBaseModel::class.java)
                _viewState.value = SignatureUploadViewState.ShowExistingExistingSignature(
                    verificationData?.signature?.fullSignatureUrl?.toUri()
                )
            } else {
                _viewState.value = SignatureUploadViewState.ShowExistingExistingSignature(
                    null
                )
            }

        } catch (e: Exception) {
            _viewState.value = SignatureUploadViewState.ErrorWhileCheckingExsitingSignature(
                e.message ?: "Unable to check existing signature"
            )

            logger.e(
                TAG,
                "while fetching verification for user : $userId",
                e
            )
        }
    }

    fun handleEvent(
        event: SignatureViewEvents
    ) {
        when (event) {
            SignatureViewEvents.DoneOrCancelButtonClicked -> checkIfUserClickedImageOrNot()
            is SignatureViewEvents.SignatureCaptured -> checkedIfBackgroundNeedsToBeRemoved(
                event.signature
            )
        }
    }

    private fun checkIfUserClickedImageOrNot() = viewModelScope.launch {
        if (processedImageUri != null) {
            backgroundRemoved = false
            uploadImage(processedImageUri!!)
        } else {
            _viewState.value = SignatureUploadViewState.NavigateBackToPreviousScreen
        }
    }

    private fun checkedIfBackgroundNeedsToBeRemoved(
        signatureUnProcessed: Uri
    ) = viewModelScope.launch {

        if (shouldRemoveBackgroundFromSignature) {

            backgroundRemoved = false
            removeBackgroundFromSignature(signatureUnProcessed)
            backgroundRemoved = true
        } else {

            processedImageUri = signatureUnProcessed
            _viewState.value = SignatureUploadViewState.BackgroundRemovedFromSignature(
                signatureUnProcessed,
                true
            )
        }
    }

    private suspend fun removeBackgroundFromSignature(
        unprocessedSignature: Uri
    ) {
        _viewState.value = SignatureUploadViewState.RemovingBackgroundFromSignature

        try {
            delay(4000) //to

//            val uploadSignatureResponse = signatureRepository.removeBackgroundFromSignature(unprocessedSignature)
            _viewState.value = SignatureUploadViewState.BackgroundRemovedFromSignature(
                unprocessedSignature,
                true
            )
        } catch (e: Exception) {
            _viewState.value = SignatureUploadViewState.ErrorWhileRemovingBackgroundFromSignature(
                unprocessedSignature
            )
        }
    }

    private suspend fun uploadImage(
        uri: Uri
    ) {
        _viewState.value = SignatureUploadViewState.UploadingSignature

        try {

            val uploadSignatureResponse = signatureRepository.uploadSignature(
                uri,
                userId
            )
            _viewState.value = SignatureUploadViewState.SignatureUploaded(
                uploadSignatureResponse.signatureFirebasePath,
                uploadSignatureResponse.signatureFullUrl
            )
        } catch (e: Exception) {

            _viewState.value = SignatureUploadViewState.ErrorUploadingSignatureImage(
                error = e.message ?: "Unable to upload or process signature image"
            )
        }
    }


}