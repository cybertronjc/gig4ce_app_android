package com.gigforce.common_image_picker.image_capture_camerax


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.logger.GigforceLogger
import java.io.File

sealed class CaptureImageSharedViewState {

    data class ImageCaptured(
        val image: File,
        val orientation: Int,
        val depth: Boolean
    ) : CaptureImageSharedViewState()

    object CameraPermissionGranted : CaptureImageSharedViewState()

    object CapturedImageDiscarded : CaptureImageSharedViewState()

    data class CapturedImageApproved(
        val image: File,
        val uploadedPathInFirebaseStorageIfUploaded: String?
    ) : CaptureImageSharedViewState()

}

class CaptureImageSharedViewModel : ViewModel() {



    private val _captureImageSharedViewModelState: MutableLiveData<CaptureImageSharedViewState> =
        MutableLiveData()
    val captureImageSharedViewModelState: LiveData<CaptureImageSharedViewState> =
        _captureImageSharedViewModelState


    fun imageCapturedOpenImageViewer(
        output: File,
        orientation: Int,
        depth: Boolean
    ) {
        _captureImageSharedViewModelState.value = CaptureImageSharedViewState.ImageCaptured(
            image = output,
            orientation = orientation,
            depth = depth
        )
    }

    fun allPermissionGranted() {
        _captureImageSharedViewModelState.value =
            CaptureImageSharedViewState.CameraPermissionGranted
    }

    fun clickedImageDiscarded() {
        _captureImageSharedViewModelState.value = CaptureImageSharedViewState.CapturedImageDiscarded
    }

    fun clickedImageUploadedSuccessFully(
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) {
        _captureImageSharedViewModelState.postValue( CaptureImageSharedViewState.CapturedImageApproved(
            file,
            parentDirectoryNameInFirebaseStorage
        ))
    }


}