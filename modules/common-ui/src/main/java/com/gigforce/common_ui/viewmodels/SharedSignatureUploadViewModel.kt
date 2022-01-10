package com.gigforce.verification.mainverification.signature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


sealed class SharedSignatureUploadViewModelViewState {

    data class SignatureCaptured(
        val pathOnFirebase: String,
        val imageFullUrl: String
    ) : SharedSignatureUploadViewModelViewState()
}

class SharedSignatureUploadViewModel : ViewModel() {

    private val _viewState = MutableSharedFlow<SharedSignatureUploadViewModelViewState>()
    val viewState = _viewState.asSharedFlow()

    fun signatureCapturedAndUploaded(
        imagePathOnFirebase: String,
        imageFullUrl: String
    ) = viewModelScope.launch {
        _viewState.emit(
            SharedSignatureUploadViewModelViewState.SignatureCaptured(
                imagePathOnFirebase,
                imageFullUrl
            )
        )
    }
}