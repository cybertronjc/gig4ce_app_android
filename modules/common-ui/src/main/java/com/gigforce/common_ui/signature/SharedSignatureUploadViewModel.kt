package com.gigforce.common_ui.signature

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.configrepository.SignatureRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


sealed class SharedSignatureUploadViewModelViewState {

    data class SignatureCaptured(
        val pathOnFirebase : String
    ) : SharedSignatureUploadViewModelViewState()
}

class SharedSignatureUploadViewModel : ViewModel() {

    private val _viewState = MutableSharedFlow<SharedSignatureUploadViewModelViewState>()
    val viewState  = _viewState.asSharedFlow()

    fun signatureCapturedAndUploaded(
        imagePathOnFirebase : String
    ) = viewModelScope.launch{
        _viewState.emit(SharedSignatureUploadViewModelViewState.SignatureCaptured(imagePathOnFirebase))
    }
}