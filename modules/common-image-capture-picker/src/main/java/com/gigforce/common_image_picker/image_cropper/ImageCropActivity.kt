package com.gigforce.common_image_picker.image_cropper

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.canhub.cropper.CropImageView
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.databinding.ActivityImageCropBinding
import com.gigforce.core.logger.GigforceLogger
import com.yalantis.ucrop.util.FileUtils.getDataColumn
import dagger.hilt.android.AndroidEntryPoint
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject


@AndroidEntryPoint
class ImageCropActivity : AppCompatActivity() {

    companion object {
        const val PREFIX: String = "IMG"
        const val EXTENSION: String = ".jpg"
        const val CROP_RESULT_CODE = 90

        const val CROPPED_IMAGE_URL_EXTRA = "CROPPED_IMAGE_URL_EXTRA"

        /**
         *
         */
        const val INTENT_EXTRA_DESTINATION_URI = "destination_uri"

        /**
         * Enable or disbale 1:1 crop window
         */
        const val INTENT_EXTRA_ENABLE_FREE_CROP = "enable_free_crop"
    }

    private lateinit var viewBinding: ActivityImageCropBinding
    private var cropImageUri: Uri? = null
    private lateinit var incomingFile: String
    private var enableFreeCropping : Boolean = false

    @Inject
    lateinit var logger: GigforceLogger
    private var win: Window? = null
    private var destinationUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        //this.setContentView(R.layout.activity_image_crop)
        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_image_crop)
        //getDataFromIntent(savedInstanceState)
        val extras = intent.extras
        if (extras != null) {
            incomingFile = extras.getString("outgoingUri").toString()
            destinationUri = extras.getParcelable(INTENT_EXTRA_DESTINATION_URI)
            enableFreeCropping = extras.getBoolean(INTENT_EXTRA_ENABLE_FREE_CROP)
        }
        changeStatusBarColor()
        setIncomingImage(incomingFile)
        initListeners()

    }

    private fun initListeners() = viewBinding.apply {


        //this will rotate the image by 90 degree in anticlockwise
        rotateImg.setOnClickListener {
            rotateImageAntiClock()
        }

        closeImg.setOnClickListener {
            onBackPressed()
        }

        okayImg.setOnClickListener {
            viewBinding.progressCircular.visibility = View.VISIBLE
            val cropedImage = cropImageView.getCroppedImage(1200, 1600) //720p

            if(cropedImage != null)
                saveBitmapToDestinationUriAndReturnResults(cropedImage)

        }

        cropImageView.setOnSetImageUriCompleteListener(object :
            CropImageView.OnSetImageUriCompleteListener {
            override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
                viewBinding.progressCircular.visibility = View.GONE
                Log.d("uri", "uri : $uri")
            }

        })


        backImageButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun changeStatusBarColor() {
        win = this.window
        // clear FLAG_TRANSLUCENT_STATUS flag:
        win?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        win?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        win?.setStatusBarColor(resources.getColor(R.color.status_bar_pink))
    }

    private fun setIncomingImage(incomingFile: String) = viewBinding.apply {
        if (incomingFile.isNotEmpty()) {
            val incomingUri: Uri = Uri.parse(incomingFile)
            //logger.d(ImageCropFragment.logTag, "incomingFile uri : $incomingUri")
            viewBinding.progressCircular.visibility = View.VISIBLE
            cropImageUri = incomingUri
            cropImageView.setImageUriAsync(incomingUri)
            setCropImageControls()
        } else {
            //showToast("Invalid incoming image")
            Toast.makeText(this@ImageCropActivity, "Invalid incoming image", Toast.LENGTH_SHORT)
        }

    }

    private fun rotateImageAntiClock() {
        viewBinding.cropImageView.rotateImage(90)
    }

    private fun setCropImageControls() {

        viewBinding.cropImageView.apply {

            if(!enableFreeCropping) {
                setAspectRatio(1, 1)
                setFixedAspectRatio(true)
            }
            setCenterMoveEnabled(true)
            isShowProgressBar = true
            setBackgroundColor(resources.getColor(R.color.warm_grey))
            setMultiTouchEnabled(false)
        }
    }


    private fun handleCropResult(result: CropImageView.CropResult?) {
        if (result != null && result?.error == null) {
//            val imageBitmap =
//                if (viewBinding.cropImageView.cropShape == CropImageView.CropShape.OVAL)
//                    result.bitmap?.let { CropImage.toOvalBitmap(it) }
//                else result.bitmap


//            Log.v("File result", "result : ${result?.bitmap.toString()} , ${result?.cropRect.toString()}, ${result.uriContent.toString()} , :bimap , ${result.error}" )
//
//            //logger.d(ImageCropFragment.logTag, "cropped result  : ${result?.uriContent.toString() }")
//            //SCropResultActivity.start(this, imageBitmap, result.uriContent, result.sampleSize)
//            getUriFromBitmap(imageBitmap)?.let {
//
//            }
//            result?.uriContent?.let {
//                saveBitmapToDestinationUriAndReturnResults(it)
//            }

            //Log.v("File Path temp", "filepath ${getUriFromBitmap(imageBitmap)} ")

        } else {
            Log.e("AIC", "Failed to crop image", result?.error)
            //logger.d(ImageCropFragment.logTag, "Failed to crop image", result?.error.toString())

            Toast.makeText(
                this@ImageCropActivity,
                "Crop failed: ${result?.error?.message}",
                Toast.LENGTH_SHORT
            )
        }
    }

    private fun saveBitmapToDestinationUriAndReturnResults(
        image: Bitmap
    ) {
        val savedImageUri  = saveBitmapToUri(image)
        val resultIntent = Intent()
        resultIntent.putExtra(CROPPED_IMAGE_URL_EXTRA, savedImageUri.toString())
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

    private fun getUriFromContentUri(uri: Uri): Uri? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFileName = "IMG" + "_" + timeStamp + "_"
        var inputStream = contentResolver.openInputStream(uri)
        val destinationFileUri = if (destinationUri == null) {
            File(filesDir, imageFileName + ".jpg").toUri()
        } else {
            destinationUri!!
        }
        var bis: BufferedInputStream? = null
        var bos: BufferedOutputStream? = null

        try {
            bis = BufferedInputStream(inputStream)
            bos = BufferedOutputStream(contentResolver.openOutputStream(destinationFileUri))
            val buf = ByteArray(1024)
            bis.read(buf)
            do {
                bos.write(buf)
            } while (bis.read(buf) !== -1)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                if (bis != null) bis.close()
                if (bos != null) bos.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return destinationFileUri
    }

    private fun saveBitmapToUri(bitmap: Bitmap): Uri {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())

        val imageFileName = "IMG" + "_" + timeStamp + "_"
        val destinationFileUri = if (destinationUri == null) {
            File(filesDir, imageFileName + ".jpg").toUri()
        } else {
            destinationUri!!
        }

        try {
            BufferedOutputStream(contentResolver.openOutputStream(destinationFileUri)).use {
                bitmap.compress(
                    Bitmap.CompressFormat.JPEG,
                    100,
                    it
                )
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {

            try {
                if (!bitmap.isRecycled)
                    bitmap.recycle()
            } catch (e: Exception) {
            }
        }

        return destinationFileUri
    }


    fun getRealPathFromURI(contentUri: Uri?): String? {
        var path: String? = null
        val proj = arrayOf<String>(MediaStore.MediaColumns.DATA)
        val cursor: Cursor? = contentResolver.query(contentUri!!, proj, null, null, null)
        if (cursor?.moveToFirst() == true) {
            val column_index: Int = cursor?.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
            path = cursor?.getString(column_index)
        }
        cursor?.close()
        return path
    }


    private fun getDefaultUri(): Uri? {
        var tempDir: File = Environment.getExternalStorageDirectory()
        tempDir = File(tempDir.getAbsolutePath().toString() + "/.temp/")
        tempDir.mkdir()
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"

        val tempFile: File = File.createTempFile(imageFileName, ".jpg", tempDir)

        return Uri.fromFile(tempFile)
    }

    private fun getUriFromBitmap(bitmap: Bitmap?): Uri? {

        var tempDir: File = Environment.getExternalStorageDirectory()
        tempDir = File(tempDir.getAbsolutePath().toString() + "/.temp/")
        tempDir.mkdir()
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"

        val tempFile: File = File.createTempFile(imageFileName, ".jpg", tempDir)
        val bytes = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val bitmapData: ByteArray = bytes.toByteArray()

        //write the bytes in file

        //write the bytes in file
        val fos = FileOutputStream(tempFile)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
        return Uri.fromFile(tempFile)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage("Are you sure you want to close?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun getRealPath(uri: Uri?): String? {
        val docId: String = DocumentsContract.getDocumentId(uri)
        val split = docId.split(":".toRegex()).toTypedArray()
        val type = split[0]
        val contentUri: Uri
        contentUri = when (type) {
            "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            else -> MediaStore.Files.getContentUri("external")
        }
        val selection = "_id=?"
        val selectionArgs = arrayOf(
            split[1]
        )

        return getDataColumn(this, contentUri, selection, selectionArgs)
    }

}
