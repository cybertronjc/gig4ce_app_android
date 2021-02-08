package com.gigforce.common_image_picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.yalantis.ucrop.UCrop
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraAndGalleryIntegrator : ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    private var activity: FragmentActivity
    private var context: Context
    private var fragment: Fragment? = null

    constructor(activity: FragmentActivity) {
        this.activity = activity
        this.context = activity
    }

    constructor(fragment: Fragment) {
        this.context = fragment.requireContext()
        this.fragment = fragment
        this.activity = fragment.requireActivity()
    }

    private val options = with(FirebaseVisionFaceDetectorOptions.Builder()) {
        setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
        setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        setMinFaceSize(0.15f)
        setTrackingEnabled(true)
        build()
    }

    private val detector = FirebaseVision.getInstance().getVisionFaceDetector(options)

    fun showCameraAndGalleryBottomSheet(
    ) {

        val fragmentManager: FragmentManager = if (fragment != null) {
            fragment!!.childFragmentManager
        } else {
            activity.supportFragmentManager
        }

        ClickOrSelectImageBottomSheet.launch(fragmentManager, this)
    }

    fun startCameraForCapturing() {

        val context: Context = if (fragment != null) {
            fragment!!.requireContext()
        } else {
            activity
        }

        val intents = ImagePicker.getCaptureImageIntentsOnly(context)
        if (fragment != null) {
            fragment!!.startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
        } else {
            activity.startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
        }
    }

    fun startGalleryForPicking() {

        val context: Context = if (fragment != null) {
            fragment!!.requireContext()
        } else {
            activity
        }

        val intents = ImagePicker.getPickImageIntentsOnly(context)
        if (fragment != null) {
            fragment!!.startActivityForResult(intents, REQUEST_PICK_IMAGE)
        } else {
            activity.startActivityForResult(intents, REQUEST_PICK_IMAGE)
        }
    }

    fun parseResults(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
        imageCropOptions: ImageCropOptions,
        callback: ImageCropCallback
    ) {

        val context: Context = if (fragment != null) {
            fragment!!.requireContext()
        } else {
            activity
        }

        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(context, resultCode, data)

            if (outputFileUri == null) {

                callback.errorWhileCapturingOrPickingImage(
                    Exception("Unable to capture results")
                )
            } else {

                if (imageCropOptions.shouldOpenImageCropper) {
                    startImageCropper(outputFileUri, imageCropOptions)
                } else if (imageCropOptions.shouldDetectForFace) {
                    val fVisionImage = FirebaseVisionImage.fromFilePath(context, outputFileUri)
                    detectFacesAndReturnResult(callback, outputFileUri, fVisionImage)
                } else {
                    returnFileImage(callback, outputFileUri)
                }
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            Log.d("ImageUri", imageUriResultCrop.toString())

            if (imageUriResultCrop == null) {
                callback.errorWhileCapturingOrPickingImage(
                    Exception(
                        "Unable to capture or pick Image"
                    )
                )

                FirebaseCrashlytics.getInstance().apply {
                    log("Got no results from imagecrop")
                    recordException(Exception("imageUriResultCrop found null from image cropping library"))
                }
            } else {
                if (imageCropOptions.shouldDetectForFace) {

                    val fvImage = FirebaseVisionImage.fromFilePath(context, imageUriResultCrop)
                    detectFacesAndReturnResult(callback, imageUriResultCrop, fvImage)
                } else
                    returnFileImage(callback, imageUriResultCrop)
            }
        }
    }

    private fun returnFileImage(callback: ImageCropCallback, outputFileUri: Uri) {
        callback.imageResult(outputFileUri)
    }

    private fun startImageCropper(uri: Uri, imageCropOptions: ImageCropOptions) {
        Log.v("Start Crop", "started")
        //can use this for a new name every time

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFileName = "IMG_${timeStamp}_"
        val uCrop: UCrop = if (imageCropOptions.outputFileUri == null) {
            UCrop.of(
                uri,
                Uri.fromFile(File(context.cacheDir, imageFileName + EXTENSION))
            )
        } else {
            UCrop.of(
                uri,
                imageCropOptions.outputFileUri!!
            )
        }
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())

        if (fragment != null) {
            uCrop.start(fragment!!.requireContext(), fragment!!)
        } else {
            uCrop.start(activity as AppCompatActivity)
        }
    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
//        options.setMaxBitmapSize(1000)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(false)
        options.setStatusBarColor(
            ResourcesCompat.getColor(
                context.resources,
                R.color.topBarDark,
                null
            )
        )
        options.setToolbarColor(
            ResourcesCompat.getColor(
                context.resources,
                R.color.topBarDark,
                null
            )
        )
        options.setToolbarTitle(context.getString(R.string.crop_and_rotate))
        return options
    }

    private fun detectFacesAndReturnResult(
        callback: ImageCropCallback,
        outputFileUri: Uri,
        firebaseVisionImage: FirebaseVisionImage
    ) = detector.detectInImage(firebaseVisionImage).addOnSuccessListener { faces ->
        // Task completed successfully

        if (faces.size > 0) {
            returnFileImage(callback, outputFileUri)
        } else {
            callback.errorWhileCapturingOrPickingImage(
                Exception(
                    "No Face Detected"
                )
            )
        }
    }.addOnFailureListener { e ->
        // Task failed with an exception
        Log.d("CStatus", "Face detection failed! still uploading the image")
        FirebaseCrashlytics.getInstance().apply {
            log("Unable to detect face")
            recordException(e)
        }

        //just return image
        callback.imageResult(outputFileUri)
    }


    companion object {
        const val REQUEST_CAPTURE_IMAGE = 101
        const val REQUEST_PICK_IMAGE = 102
        const val REQUEST_CROP = 69

        const val EXTENSION = ".jpg"
    }

    override fun onClickPictureThroughCameraClicked() {
        startCameraForCapturing()
    }

    override fun onPickImageThroughCameraClicked() {
        startGalleryForPicking()
    }
}