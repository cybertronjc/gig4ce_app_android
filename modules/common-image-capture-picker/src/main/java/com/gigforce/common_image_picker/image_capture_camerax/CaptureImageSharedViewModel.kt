package com.gigforce.common_image_picker.image_capture_camerax


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.*
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
    object DetectingFace : CaptureImageSharedViewState()

    object FaceDetected : CaptureImageSharedViewState()

    data class ErrorWhileFaceDetection(
        val message : String
    ) : CaptureImageSharedViewState()

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
        context: Context,
        shouldUploadImageToo: Boolean,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) = viewModelScope.launch(Dispatchers.IO) {

        var uploadPathInFirebaseStorage: String? = null
        if (shouldUploadImageToo) {

            _captureImageSharedViewModelState.postValue(CaptureImageSharedViewState.ImageUploading(0))
            try {

                val bitmapFactoryOptions = BitmapFactory.Options().apply {
                    this.inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(file.absolutePath, bitmapFactoryOptions)
                val requiredOutputSize = calculateRequiredOutputSize(
                    bitmapFactoryOptions.outWidth,
                    bitmapFactoryOptions.outHeight
                )

                val finalImage = try {
                    Glide.with(context)
                        .asBitmap()
                        .load(file)
                        .submit(requiredOutputSize.first, requiredOutputSize.second)
                        .get()
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                } ?: return@launch

                uploadPathInFirebaseStorage = bitmapToFile(
                    context,
                    finalImage,
                    "temp.jpg"
                )?.let {
                    uploadImageInFirebase(
                        parentDirectoryNameInFirebaseStorage!!,
                        "Image",
                        it
                    )
                }

            } catch (e: Exception) {
                _captureImageSharedViewModelState.postValue(
                    CaptureImageSharedViewState.ImageUploadFailed(
                        error = e.message ?: ""
                    )
                )
            }
        }

        _captureImageSharedViewModelState.postValue(CaptureImageSharedViewState.CapturedImageApproved(
            file,
            uploadPathInFirebaseStorage
        ))
    }

    private fun calculateRequiredOutputSize(
        actualWidth: Int,
        actualHeight: Int
    ): Pair<Int, Int> {

        //      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 720.0f
        val maxWidth = 720.0f

        var outputWidth: Int = 512
        var outputHeight: Int = 512
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

        //      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                outputWidth = (imgRatio * actualWidth).toInt()
                outputHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                outputHeight = (imgRatio * actualHeight).toInt()
                outputWidth = maxWidth.toInt()
            } else {
                outputHeight = maxHeight.toInt()
                outputWidth = maxWidth.toInt()
            }
        }

        return outputWidth to outputHeight
    }


    private fun bitmapToFile(
        context: Context,
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(context.filesDir, fileNameToSave)
            file.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 75, bos) // YOU can also save it in JPEG
            recycleBitmap(bitmap)

            val bitmapdata = bos.toByteArray()

            //write the bytes in file
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
            file
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            file // it will return null
        } finally {
            recycleBitmap(bitmap)
        }
    }

    private fun recycleBitmap(bitmap: Bitmap) {
        try {
            if (!bitmap.isRecycled) {
                bitmap.recycle()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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


//        val upload = mReference.putBytes(file).addOnProgressListener {
//            val progress = (100.0 * it.bytesTransferred) / it.totalByteCount
//            _captureImageSharedViewModelState.value = CaptureImageSharedViewState.ImageUploading(progress.toInt())
//        }
        val uploadResult = mReference.putFileOrThrow(
            Uri.fromFile(file)
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