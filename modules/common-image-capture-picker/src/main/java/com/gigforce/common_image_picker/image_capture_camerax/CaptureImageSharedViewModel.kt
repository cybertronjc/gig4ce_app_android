package com.gigforce.common_image_picker.image_capture_camerax

import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.provider.MediaStore
import android.net.Uri
import android.os.Environment

import android.util.Log
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.extensions.putBytesOrThrow
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.OnProgressListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
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

    val options = with(FirebaseVisionFaceDetectorOptions.Builder()) {
        setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
        setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        setMinFaceSize(0.15f)
        setTrackingEnabled(true)
        build()
    }
    val detector = FirebaseVision.getInstance()
        .getVisionFaceDetector(options)

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
                val filePath = file.absolutePath
                Log.d("path", "path: $filePath")
                val bmOptions = BitmapFactory.Options()
                val bitmap = BitmapFactory.decodeFile(filePath, bmOptions)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, baos)

                val actualWidth: Int = bitmap.getWidth()
                val actualHeight: Int = bitmap.getHeight()
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
                val originalBitmapCount = bitmap.allocationByteCount
                Log.d("count", "original $originalBitmapCount")

                var resizedBitmap = Bitmap.createScaledBitmap(bitmap, outputWidth, outputHeight, false)
//      check the rotation of the image and display it properly
                val resizedBitmapCount = resizedBitmap.allocationByteCount
                Log.d("count", "original $resizedBitmapCount")
                val exif: ExifInterface?
                try {
                    exif = filePath?.let { ExifInterface(it) }
                    val orientation: Int? = exif?.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION, 0
                    )
                    Log.d("EXIF", "Exif: $orientation")
                    val matrix = Matrix()
                    if (orientation == 6) {
                        matrix.postRotate(90F)
                        Log.d("EXIF", "Exif: $orientation")
                    } else if (orientation == 3) {
                        matrix.postRotate(180F)
                        Log.d("EXIF", "Exif: $orientation")
                    } else if (orientation == 8) {
                        matrix.postRotate(270F)
                        Log.d("EXIF", "Exif: $orientation")
                    }
                    resizedBitmap = Bitmap.createBitmap(
                        resizedBitmap!!, 0, 0,
                        resizedBitmap.width, resizedBitmap.height, matrix,
                        true
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                }



                //detecting face
//                val fvImage = FirebaseVisionImage.fromByteArray(baos.toByteArray(), )
//                //  Face detect - Check if face is present in the cropped image or not.
//                val result = detector.detectInImage(fvImage)
//                    .addOnSuccessListener { faces ->
//                        // Task completed successfully
//                        if (faces.size > 0) {
//                            Log.d("FaceDetect", "success")
//
//                        } else {
//                          Log.d("FaceDetect", "failed")
//                        }
//                    }
//                    .addOnFailureListener { e ->
//                        // Task failed with an exception
//                        Log.d("FaceDetect", "failed ${e.message}")
//                    }

                uploadPathInFirebaseStorage = bitmapToFile(resizedBitmap, "temp.jpg")?.let {
                    uploadImageInFirebase(
                        parentDirectoryNameInFirebaseStorage!!,
                        "Image",
                        it
                    )
                }

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

    fun bitmapToFile(
        bitmap: Bitmap,
        fileNameToSave: String
    ): File? { // File name like "image.png"
        //create a file to write bitmap data
        var file: File? = null
        return try {
            file = File(
                Environment.getExternalStorageDirectory()
                    .toString() + File.separator + fileNameToSave
            )
            file!!.createNewFile()

            //Convert bitmap to byte array
            val bos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 75, bos) // YOU can also save it in JPEG
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