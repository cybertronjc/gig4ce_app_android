package com.gigforce.common_ui.viewmodels.signature

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch


sealed class SharedSignatureViewModelState {

    data class SignatureImageUploaded(
        val signaturePathOnFirebase : String
    ) : SharedSignatureViewModelState()
}
class SharedSignatureViewModel : ViewModel() {

    private val _viewState = MutableSharedFlow<SharedSignatureViewModelState>()
    val viewState = _viewState.asSharedFlow()

    fun signatureUploaded(
        imagePathOnFirebase : String
    ) = viewModelScope.launch{
        _viewState.emit(SharedSignatureViewModelState.SignatureImageUploaded(imagePathOnFirebase))
    }


}