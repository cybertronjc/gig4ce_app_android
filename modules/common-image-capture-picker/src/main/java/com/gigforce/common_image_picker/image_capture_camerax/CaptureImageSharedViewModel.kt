package com.gigforce.common_image_picker.image_capture_camerax

import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

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

    //Image Upload

    data class ImageUploading(
        val progress : Int
    ) : CaptureImageSharedViewState()

    data class ImageUploadFailed(
        val error: String
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

    fun clickedImageApproved(
        shouldUploadImageToo: Boolean,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) = viewModelScope.launch {

        var uploadPathInFirebaseStorage: String? = null
        if (shouldUploadImageToo) {

            _captureImageSharedViewModelState.value = CaptureImageSharedViewState.ImageUploading(0)
            try {
                uploadPathInFirebaseStorage =  uploadImageInFirebase(
                    parentDirectoryNameInFirebaseStorage!!,
                    "Image",
                    file
                )
            } catch (e: Exception) {
                _captureImageSharedViewModelState.value = CaptureImageSharedViewState.ImageUploadFailed(
                    error = e.message ?: ""
                )
            }
        }

        _captureImageSharedViewModelState.value = CaptureImageSharedViewState.CapturedImageApproved(
            file,
            uploadPathInFirebaseStorage
        )
    }

    private suspend fun uploadImageInFirebase(
        parentDirName: String,
        imageName: String,
        file: File
    ): String {
        val selfieImg = getTimeStampAsName() + getTimeStampAsName() + ".jpg"
        val mReference = FirebaseStorage.getInstance().reference
            .child(parentDirName)
            .child(selfieImg)


        val uploadResult = mReference.putFileOrThrow(
            file.toUri()
        ) { progressSnapShot ->
            val progress = (100.0 * progressSnapShot.bytesTransferred) / progressSnapShot.totalByteCount
            _captureImageSharedViewModelState.value = CaptureImageSharedViewState.ImageUploading(progress.toInt())
        }

        return uploadResult.metadata?.path ?: mReference.path
    }

    private fun getTimeStampAsName(): String {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return timeStamp
    }

    private suspend fun StorageReference.putFileOrThrow(
        file: Uri,
        progressListener: OnProgressListener<UploadTask.TaskSnapshot>?
    ) =
        suspendCancellableCoroutine<UploadTask.TaskSnapshot> { cont ->
            val putFileTask = putFile(file)

            if (progressListener != null) {
                putFileTask.addOnProgressListener(progressListener)
            }

            cont.invokeOnCancellation {
                if (!putFileTask.isComplete)
                    putFileTask.cancel()
            }

            putFileTask
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resumeWithException(it) }
        }


}