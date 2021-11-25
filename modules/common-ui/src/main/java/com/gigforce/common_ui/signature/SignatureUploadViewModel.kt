package com.gigforce.common_ui.signature

import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.MimeTypes
import com.gigforce.common_ui.configrepository.SignatureRepository
import com.gigforce.core.date.DateHelper
import com.gigforce.core.utils.Lce
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.net.URL
import javax.inject.Inject

sealed class SignatureUploadViewState {

    object UploadingSignature : SignatureUploadViewState()

    data class SignatureUploaded (
        val processedImage : URL
    ) : SignatureUploadViewState()

    data class ErrorUploadingOrProcessingSignatureImage(
        val error : String
    ) : SignatureUploadViewState()
}

@HiltViewModel
class SignatureUploadViewModel @Inject constructor(
    private val firebaseStorage: FirebaseStorage,
    private val signatureRepository: SignatureRepository
): ViewModel() {

    companion object{
        private const val DIRECTORY_SIGNATURES = "Signatures"
    }

    private val _viewState = MutableLiveData<SignatureUploadViewState>()
    val viewState : LiveData<SignatureUploadViewState> = _viewState

    fun uploadImage(
        uri : Uri
    ) = viewModelScope.launch {
        _viewState.value = SignatureUploadViewState.UploadingSignature

        try {
           val uploadSignatureResponse =  signatureRepository.uploadSignatureImageAndGetProcessImageUrl(
                uri
            )

            _viewState


        } catch (e: Exception) {
            _viewState.value = SignatureUploadViewState.ErrorUploadingOrProcessingSignatureImage(
                error = e.message ?: "Unable to upload or process signature image"
            )
        }
    }

    private fun createImageFile(
        mimeType: String = MimeTypes.PNG
    ): String {
        val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        return "IMG-${DateHelper.getFullDateTimeStamp()}.$extension"
    }

}