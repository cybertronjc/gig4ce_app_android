package com.gigforce.app.modules.markattendance

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.otaliastudios.cameraview.BitmapCallback
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.activity_picture_preview.*
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class ImageCaptureActivity : AppCompatActivity() {
    var pictureResult: PictureResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_picture_preview)
        initCamera()
        listener()
    }

    private fun listener() {
        capture_icon.setOnClickListener {

            if(!show_img_cl.isVisible) {
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
        var compressByteArray =
            bitmapToByteArray(getResizedBitmap(byteArrayToBitmap(pictureResult!!.data), 750))
        var selfieImg = getTimeStampAsName() + getTimeStampAsName() + ".jpg"
        var mReference = FirebaseStorage.getInstance().reference
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
                    show_pic.scaleType =
                        if (it?.width!! > it.height) ImageView.ScaleType.FIT_CENTER else ImageView.ScaleType.CENTER_CROP
                    show_pic.setImageBitmap(it)
//                    show_pic_bg.setImageBitmap(it)
                });
            } catch (e: UnsupportedOperationException) {
                show_pic.setImageDrawable(ColorDrawable(Color.GREEN));
                showToast("Can't preview this format: ");
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
        var width = image.getWidth()
        var height = image.getHeight()
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
        var options = BitmapFactory.Options();
        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options);
    }

    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        var stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
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
}