package com.gigforce.common_ui.signature

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.configrepository.SignatureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SignatureViewEvents {

    data class SignatureCaptured(
        val signature: Uri
    ) : SignatureViewEvents()

    object DoneOrCancelButtonClicked : SignatureViewEvents()
}

sealed class SignatureUploadViewState {

    object RemovingBackgroundFromSignature : SignatureUploadViewState()

    data class BackgroundRemovedFromSignature(
        val processedImage: Uri
    ) : SignatureUploadViewState()

    data class ErrorWhileRemovingBackgroundFromSignature(
        val processedImage: Uri
    ) : SignatureUploadViewState()

    object UploadingSignature : SignatureUploadViewState()

    data class SignatureUploaded(
        val firebaseCompletePath: String
    ) : SignatureUploadViewState()

    data class ErrorUploadingSignatureImage(
        val error: String
    ) : SignatureUploadViewState()

    data class SignatureUploadCompletedOrCancelled(
        val signature: String?
    ) : SignatureUploadViewState()
}

@HiltViewModel
class SignatureUploadViewModel @Inject constructor(
    private val signatureRepository: SignatureRepository
) : ViewModel() {


    private val _viewState = MutableLiveData<SignatureUploadViewState>()
    val viewState: LiveData<SignatureUploadViewState> = _viewState

    private var processedImageUri : Uri? = null
    var shouldRemoveBackgroundFromSignature: Boolean = false

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
        if(processedImageUri != null){
            uploadImage(processedImageUri!!)
        } else{
            _viewState.value = SignatureUploadViewState.SignatureUploadCompletedOrCancelled(
                null
            )
        }
    }

    private fun checkedIfBackgroundNeedsToBeRemoved(
        signatureUnProcessed: Uri
    ) = viewModelScope.launch{

        if (shouldRemoveBackgroundFromSignature) {
            removeBackgroundFromSignature(signatureUnProcessed)
        } else {

            processedImageUri = signatureUnProcessed
            _viewState.value = SignatureUploadViewState.BackgroundRemovedFromSignature(
                signatureUnProcessed
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
                unprocessedSignature
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

            val uploadSignatureResponse = signatureRepository.uploadSignatureImageToFirebase(uri)
            _viewState.value = SignatureUploadViewState.SignatureUploaded(uploadSignatureResponse)
        } catch (e: Exception) {

            _viewState.value = SignatureUploadViewState.ErrorUploadingSignatureImage(
                error = e.message ?: "Unable to upload or process signature image"
            )
        }
    }
}