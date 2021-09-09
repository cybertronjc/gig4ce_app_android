package com.gigforce.common_image_picker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
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
    private var openFrontCamera: Boolean = false

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

    fun openFrontCamera() {
        this.openFrontCamera = true
    }

    fun showCameraAndGalleryBottomSheet(
    ) {

        val fragmentManager: FragmentManager = if (fragment != null) {
            fragment!!.childFragmentManager
        } else {
            activity.supportFragmentManager
        }

        ClickOrSelectImageBottomSheet.launch(fragmentManager, false, this)
    }

    fun startCameraForCapturing() {

        val context: Context = if (fragment != null) {
            fragment!!.requireContext()
        } else {
            activity
        }

        val intents = ImagePicker.getCaptureImageIntentsOnly(context) ?: return
        if (openFrontCamera) {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && Build.VERSION.SDK_INT < Build.VERSION_CODES.O -> {
                    intents.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT)  // Tested on API 24 Android version 7.0(Samsung S6)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
                    intents.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT) // Tested on API 27 Android version 8.0(Nexus 6P)
                    intents.putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
                }
                else -> intents.putExtra("android.intent.extras.CAMERA_FACING", 1)  // Tested API 21 Android version 5.0.1(Samsung S4)
            }
        }

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

        val intents = ImagePicker.getPickImageIntentsOnly(context) ?: return
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
//                    startImageCropper(outputFileUri, imageCropOptions)
                    startCropImage(outputFileUri,imageCropOptions)
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
        else if (requestCode == ImageCropActivity.CROP_RESULT_CODE && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? =  Uri.parse(data?.getStringExtra(ImageCropActivity.CROPPED_IMAGE_URL_EXTRA))
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
                Log.d("ImageUri", "working")
                if (imageCropOptions.shouldDetectForFace) {
                    Log.d("ImageUri", "working1")
                    val fvImage = FirebaseVisionImage.fromFilePath(context, imageUriResultCrop)
                    Log.d("ImageUri", "working2")

                    detectFacesAndReturnResult(callback, imageUriResultCrop, fvImage)
                    Log.d("ImageUri", "working3")

                } else {
                    Log.d("ImageUri", "working4")

                    returnFileImage(callback, imageUriResultCrop)
                    Log.d("ImageUri", "working5")

                }
            }
        }

    }
    private fun startCropImage(
        imageUri: Uri,
        imageCropOptions: ImageCropOptions
    ) {
        val photoCropIntent = Intent(context, ImageCropActivity::class.java)
        photoCropIntent.putExtra("outgoingUri", imageUri.toString())
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_DESTINATION_URI,imageCropOptions.outputFileUri)

        val outputFileUri = if(imageCropOptions.outputFileUri == null ) {
            Uri.fromFile(File(context.cacheDir, "IMG_" + System.currentTimeMillis() + EXTENSION))
        }
        else {
            imageCropOptions.outputFileUri!!
        }
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_DESTINATION_URI,outputFileUri)
        photoCropIntent.putExtra(ImageCropActivity.INTENT_EXTRA_ENABLE_FREE_CROP,imageCropOptions.freeCropEnabled)


        if (fragment != null) {
            fragment!!.startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
        } else {
            activity.startActivityForResult(photoCropIntent, ImageCropActivity.CROP_RESULT_CODE)
        }
    }
    private fun returnFileImage(callback: ImageCropCallback, outputFileUri: Uri) {
        callback.imageResult(outputFileUri)
    }

    fun startImageCropper(uri: Uri, imageCropOptions: ImageCropOptions) {
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

        val size = getImageDimensions(uri)
        uCrop.withAspectRatio(size.width.toFloat(), size.height.toFloat())
        uCrop.withOptions(getCropOptions())

        if (fragment != null) {
            uCrop.start(fragment!!.requireContext(), fragment!!)
        } else {
            uCrop.start(activity as AppCompatActivity)
        }
    }

    private fun getImageDimensions(uri: Uri): Size {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeFile(File(uri.path).absolutePath, options)
        val imageHeight: Int = options.outHeight
        val imageWidth: Int = options.outWidth
        return Size(imageWidth, imageHeight)
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
        options.setToolbarTitle(context.getString(R.string.crop_and_rotate_common))
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

    override fun removeProfilePic() {

    }
}