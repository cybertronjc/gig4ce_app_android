package com.gigforce.app.modules.markattendance

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.otaliastudios.cameraview.BitmapCallback
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import com.otaliastudios.cameraview.controls.Mode
import com.otaliastudios.cameraview.size.SizeSelectors
import kotlinx.android.synthetic.main.activity_picture_preview.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class AttendanceImageCaptureActivity : AppCompatActivity() {
    var pictureResult: PictureResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_picture_preview)
        initCamera()
        listener()
    }

    private fun listener() {
        capture_icon.setOnClickListener {

            if (!show_img_cl.isVisible) {

                if (cameraView.mode == Mode.VIDEO) {
                    Log.e("ImageCaptureActivity", "Camera Is In Video Mode, Skipping Click Picture")
                    return@setOnClickListener
                }

                if (cameraView.isTakingPicture || cameraView.isTakingVideo)
                    return@setOnClickListener

                cameraView.takePicture()
            }
        }

        upload_img.setOnClickListener {
            retake_image.isEnabled = false
            upload_img.isEnabled = false
            uploadImage()
        }
        retake_image.setOnClickListener {
            show_img_cl.gone()
        }


    }


    var resultIntent: Intent = Intent()
    private fun uploadImage() {
        showToast("Uploading Image")
        progress_circular.visible()

        val image = File(filesDir, "capture.jpg")
        val bos = BufferedOutputStream(FileOutputStream(image))
        bos.write(pictureResult?.data)
        bos.flush()
        bos.close()
        val bitmap = getBitmap(image.absolutePath)!!
        val compressByteArray = bitmapToByteArray(getResizedBitmap(bitmap, 750))
        val selfieImg = getTimeStampAsName() + getTimeStampAsName() + ".jpg"
        val mReference = FirebaseStorage.getInstance().reference
            .child("attendance")
            .child(selfieImg)
        lateinit var uploadTask: UploadTask
//            if (uriImg != null)
//                uploadTask = mReference.putFile(uriImg)
//            else {
        uploadTask = mReference.putBytes(compressByteArray)
//            }

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
//            showToast("Uploading in progress :${progress.toInt()}% done")
            if (progress_circular != null)
                progress_circular.progress = progress.toInt()
        }
        uploadTask.addOnSuccessListener {
            showToast("Successfully uploaded - Selfie & geolocation")
            resultIntent.putExtra("image_name", selfieImg)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
//                startNavigationSliderBtn.resetSlider()
//                updateAttendanceToDB()
        }
        uploadTask.addOnFailureListener {
            showToast("Error " + it.message)
            setResult(Activity.RESULT_CANCELED, resultIntent)
            finish()
//                    startNavigationSliderBtn.resetSlider()
//                    showToast("Error " + it.message)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(Activity.RESULT_CANCELED)
    }

    private fun initCamera() {

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
//        val size = getScreenWidth(this)
//        val width = SizeSelectors.minWidth(size.width)
//        val height = SizeSelectors.minHeight(size.height)
//        val dimensions = SizeSelectors.and(width, height) // Matches sizes bigger than 1000x2000.
//        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(Size(size.width, size.height)), 0f) // Matches 1:1 sizes.
//        val result = SizeSelectors.or(
//                SizeSelectors.and(ratio, dimensions),  // Try to match both constraints
//                ratio,  // If none is found, at least try to match the aspect ratio
//                SizeSelectors.biggest() // If none is found, take the biggest
//        )
//        cameraView.setPictureSize(result)
//        cameraView.setVideoSize(result)


        FirebaseFirestore.getInstance()
            .collection("Configuration")
            .document("CameraProperties")
            .get()
            .addOnSuccessListener {

                val deviceAndDefaultSize =
                    it.getString("gig_device_and_experimental_camera_width_size") ?: ""
                val defaultSize =
                    it.getLong("gig_image_capture_default_size_selector_max_width") ?: 1000L

                initCamera2(
                    deviceAndDefaultSize,
                    defaultSize.toInt()
                )
            }.addOnFailureListener {

                initCamera2(
                    "",
                    1000
                )
            }
    }

    private fun initCamera2(
        devicesThatNeedToUseDifferentPreviewSize: String,
        defaultMaxWidth: Int
    ) {

        val deviceAndTheirMaxWidthValues = devicesThatNeedToUseDifferentPreviewSize
            .trim()
            .split(";")
            .filter { it.isNotBlank() }
            .map {
                it.substring(0, it.indexOf(":")) to it.substring(it.indexOf(":") + 1, it.length)
            }.toMap()


        if (deviceAndTheirMaxWidthValues.containsKey(Build.MODEL)) {
            val maxWidthForThisDevice = deviceAndTheirMaxWidthValues.get(Build.MODEL)?.toInt() ?: -1

            if (maxWidthForThisDevice == -1) {
                //No Restriction
            } else {
                cameraView.setPreviewStreamSize(
                    SizeSelectors.and(
                        SizeSelectors.maxWidth(
                            maxWidthForThisDevice
                        ), SizeSelectors.biggest()
                    )
                )
            }
        } else {
            cameraView.setPreviewStreamSize(
                SizeSelectors.and(
                    SizeSelectors.maxWidth(defaultMaxWidth),
                    SizeSelectors.biggest()
                )
            )
        }

        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(CameraListener())
    }

    fun showToast(str: String) {
        Toast.makeText(applicationContext, str, Toast.LENGTH_LONG).show()
    }

    private inner class CameraListener : com.otaliastudios.cameraview.CameraListener() {
        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            pictureResult = result
            previewImage(result)

        }

        private fun previewImage(result: PictureResult) {
            show_img_cl.visible()

            try {


                result.toBitmap(BitmapCallback {
                    it?.let { it1 ->
                        show_pic.scaleType =
                            if (it1.width > it1.height) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP
                        show_pic.setImageBitmap(it1)
                    }

//                    show_pic_bg.setImageBitmap(it)
                })
            } catch (e: UnsupportedOperationException) {
                show_pic.setImageDrawable(ColorDrawable(Color.GREEN))
                showToast("Can't preview this format: ")
            }
//            uploadImage(result)
//            show_pic.setImageBitmap(byteArrayToBitmap(result.data))
//            show_pic.setImageBitmap(rotateImageIfRequire(byteArrayToBitmap(result.data)))


        }
    }

    private fun getTimeStampAsName(): String {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return timeStamp
    }

    fun getResizedBitmap(image: Bitmap, maxSize: Int): Bitmap {
        var width = image.width
        var height = image.height
        val bitmapRatio = width.toFloat() / height.toFloat()
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio).toInt()
        } else {
            height = maxSize
            width = (height * bitmapRatio).toInt()
        }
        return Bitmap.createScaledBitmap(image, width, height, true)
    }

    fun byteArrayToBitmap(byteArray: ByteArray): Bitmap {
        return decodeSampledBitmapFromResource(byteArray, 720, 1280)
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)
        return stream.toByteArray()
    }

//    fun rotateImageIfRequire(bitmap: Bitmap): Bitmap {
//        if (bitmap.width > bitmap.height) {
//            return rotateImage(bitmap, -90.0F);
//        } else {
//            return bitmap
//        }
//    }

//    private fun rotateImage(img: Bitmap, degree: Float): Bitmap {
//        val matrix = Matrix()
//        matrix.postRotate(degree)
//        val rotatedImg = Bitmap.createBitmap(img, 0, 0, img.height, img.width, null, true)
//        img.recycle()
//        return rotatedImg
//    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    fun decodeSampledBitmapFromResource(
        byteArray: ByteArray,
        reqWidth: Int,
        reqHeight: Int
    ): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
        }
    }

    fun getBitmap(path: String?): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            val f = File(path)
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.ARGB_8888
            bitmap = BitmapFactory.decodeStream(FileInputStream(f), null, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}