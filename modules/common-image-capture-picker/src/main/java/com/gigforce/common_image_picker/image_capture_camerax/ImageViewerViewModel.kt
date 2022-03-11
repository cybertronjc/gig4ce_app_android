package com.gigforce.common_image_picker.image_capture_camerax


import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.ImageUtility
import com.gigforce.core.logger.GigforceLogger
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

sealed class ImageViewerViewState {

    //Image Upload
    object DetectingFace : ImageViewerViewState()

    object FaceDetected : ImageViewerViewState()

    data class ErrorWhileFaceDetection(
        val message: String
    ) : ImageViewerViewState()

    data class ImageUploading(
        val progress: Int
    ) : ImageViewerViewState()

    data class ImageUploadFailed(
        val error: String
    ) : ImageViewerViewState()

    data class ImageUploadSuccess(
        val image: File,
        val uploadedPathInFirebaseStorageIfUploaded: String?
    ) : ImageViewerViewState()

    object RotatingImage : ImageViewerViewState()

    data class ImageRotated(
        val file: File
    ) : ImageViewerViewState()

    data class ImageRotationFailed(
        val error: String
    ) : ImageViewerViewState()
}

class ImageViewerViewModel : ViewModel() {

    companion object{

        const val TAG = "ImageViewerViewModel"
    }

    private val logger = GigforceLogger()

    private val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()

    private val detector = FaceDetection.getClient(highAccuracyOpts)

    private val _viewState: MutableLiveData<ImageViewerViewState> = MutableLiveData()
    val viewState: LiveData<ImageViewerViewState> = _viewState

    private var sharedViewModel : CaptureImageSharedViewModel? = null
    private var currentlyDetectingFaces = false
    private var currentlyUploading = false

    fun setSharedViewModel(
        sharedViewModel : CaptureImageSharedViewModel
    ){
        this.sharedViewModel = sharedViewModel
    }

    fun rotateImage(
        context: Context,
        file: File
    ) = viewModelScope.launch(Dispatchers.IO) {
        if(currentlyDetectingFaces || currentlyUploading) return@launch

        _viewState.postValue(ImageViewerViewState.RotatingImage)

        try {
            val rotatedImage = ImageUtility.loadRotateAndSaveImage(
                context,
                file
            )

            if(rotatedImage == null){
                _viewState.postValue(ImageViewerViewState.ImageRotationFailed("Image Rotation Failed"))
            } else{
                _viewState.postValue(ImageViewerViewState.ImageRotated(rotatedImage))
            }

        } catch (e: Exception) {
            _viewState.postValue(ImageViewerViewState.ImageRotationFailed("Image Rotation Failed"))
        }
    }

    fun detectFaceAndUploadImage(
        context: Context,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) = viewModelScope.launch(Dispatchers.IO) {
        if(currentlyDetectingFaces || currentlyUploading) return@launch

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            detectFaceAndUploadImageOnSuccess(
                context,
                file,
                parentDirectoryNameInFirebaseStorage
            )
        } else {
            uploadImage(
                context,
                file,
                parentDirectoryNameInFirebaseStorage
            )
        }
    }

    private suspend fun detectFaceAndUploadImageOnSuccess(
        context: Context,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) {
        currentlyDetectingFaces = true
        _viewState.postValue(ImageViewerViewState.DetectingFace)

        try {
            val facesCount = detectFaceThrowOnErrorOrNoFace(
                context,
                file
            )
            _viewState.postValue(ImageViewerViewState.FaceDetected)
            currentlyDetectingFaces = false

        } catch (e: Exception) {
            currentlyDetectingFaces = false

            _viewState.postValue(
                ImageViewerViewState.ErrorWhileFaceDetection(
                    message = "unable to detect face"
                )
            )

            logger.e(TAG,"while detecting faces",e)
            return
        }

        uploadImage(
            context,
            file,
            parentDirectoryNameInFirebaseStorage
        )
    }

     fun faceNotDetectedStillUploadImage(
        context: Context,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) = viewModelScope.launch{
        uploadImage(
            context,
            file,
            parentDirectoryNameInFirebaseStorage
        )
    }

    private suspend fun uploadImage(
        context: Context,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ) {
        currentlyDetectingFaces = false
        currentlyUploading = true

        try {
            val uploadPathInFirebaseStorage = resizeAndUploadImage(
                context,
                file,
                parentDirectoryNameInFirebaseStorage
            )
            currentlyUploading = false

            _viewState.postValue(
                ImageViewerViewState.ImageUploadSuccess(
                    file,
                    uploadPathInFirebaseStorage
                )
            )

            sharedViewModel?.clickedImageUploadedSuccessFully(
                file,
                uploadPathInFirebaseStorage
            )
        } catch (e: Exception) {
            currentlyUploading = false

            _viewState.postValue(
                ImageViewerViewState.ImageUploadFailed(
                    error = e.message ?: ""
                )
            )
            logger.e(TAG,"while uploading image",e)
        }
    }

    private suspend fun resizeAndUploadImage(
        context: Context,
        file: File,
        parentDirectoryNameInFirebaseStorage: String?
    ): String {
        _viewState.postValue(ImageViewerViewState.ImageUploading(0))

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
        }

        val finalFile = if (finalImage != null) {
            ImageUtility.bitmapToFile(
                context,
                finalImage,
                "temp.jpg"
            )
        } else {
            //Uploading Bigger Image
            file
        }

        if (finalFile == null) {
            throw IllegalArgumentException("final file is null")
        }

        return uploadImageInFirebase(
            parentDirectoryNameInFirebaseStorage!!,
            "Image",
            finalFile
        )
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
            Uri.fromFile(file)
        ) { progressSnapShot ->
            val progress =
                (100.0 * progressSnapShot.bytesTransferred) / progressSnapShot.totalByteCount
            _viewState.postValue(ImageViewerViewState.ImageUploading(progress.toInt()))
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

    private suspend fun detectFaceThrowOnErrorOrNoFace(
        context: Context,
        file: File
    ) = suspendCoroutine<Int> { cont ->

        try {
            val image = InputImage.fromFilePath(
                context,
                file.toUri()
            )

            detector.process(image)
                .addOnSuccessListener { faces ->

                    if (faces.size > 0) {
                        cont.resume(faces.size)
                    } else {
                        cont.resumeWithException(
                            IllegalArgumentException("No face detected")
                        )
                    }
                }.addOnFailureListener { e ->
                    cont.resumeWithException(e)
                }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

}