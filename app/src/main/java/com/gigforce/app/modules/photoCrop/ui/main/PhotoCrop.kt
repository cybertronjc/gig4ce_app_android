package com.gigforce.app.modules.photoCrop.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class PhotoCrop : AppCompatActivity() {

    lateinit var imageFilePath: String
    private val CODE_IMG_GALLERY: Int = 1
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 1
    private val REQUEST_IMAGE_CAPTURE: Int = 1888
    private val CROPPED_IMG_NAME: String = "UploadedTest"
    private val REQUEST_TAKE_PHOTO: Int = 1
    private val EXTENSION: String = ".jpg"
    private val resultIntent: Intent = Intent()

    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        getImageFromPhone()
//        dispatchTakePictureIntent()
//        startActivityForResult(getPickImageChooserIntent(this,"title",true,true), CODE_IMG_GALLERY)
    }


    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)

        Log.e(
            "DATA_FOR_ALL",
            requestCode.toString() + " RESULT:_" + resultCode.toString() + " UCrop.REQUEST_CROP " + UCrop.REQUEST_CROP.toString() + " data= " + data
        )
        var bundle = data?.extras
        if (null != bundle) {
            for (key in bundle.keySet()!!) {
                Log.e(
                    "PHOTO_CROP_EXTRAS",
                    key + " : " + if (bundle.get(key) != null) bundle.get(key) else "NULL"
                )
            }
        }
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
//            val imageUri: Uri? = getImageUriFromBitmap(this, data?.data)
            val imageUri: Uri? = data?.data
            Log.v("COME IMG GALLERY", requestCode.toString()+" imaeURI= "+imageUri.toString())
            if (imageUri != null) {
                startCrop(imageUri)
            }
        } else if ((requestCode == UCrop.REQUEST_CROP || requestCode == REQUEST_TAKE_PHOTO) && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            print(requestCode)
            if (imageUriResultCrop != null) {
                Log.v("REQUEST CROP", requestCode.toString())
            }
            if (imageUriResultCrop != null) {
                upload(imageUriResultCrop)
//                super.finish()
            }
        }
    }

    fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri{
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }
//
//    fun getImageUri(inContext: Context, inImage: Uri?): Uri {
//        val bytes = ByteArrayOutputStream()
//        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
//        val path =
//            Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
//        return Uri.parse(path)
//    }

    open fun startCrop(uri: Uri): Unit {

        Log.v("Start Crop", "started")
        var destinationFileName: String? = CROPPED_IMG_NAME
        destinationFileName += EXTENSION

        //can use this for a new name every time

        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(cacheDir, imageFileName + EXTENSION))

            //will need for random name
//                Uri.fromFile(File.createTempFile(
//                        imageFileName,  /* prefix */
//                        EXTENSION,  /* suffix */
//                        storageDir /* directory */
//                ))
        )
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withMaxResultSize(450, 450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this as AppCompatActivity)

    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(false)
        options.setStatusBarColor(resources.getColor(R.color.topBarDark))
        options.setToolbarColor(resources.getColor(R.color.topBarDark))
        options.setToolbarTitle("Crop and Rotate")
        return options
    }

    private fun upload(uri: Uri) {

        Log.v("Upload Image", "started")
        var mReference =
            mStorage.reference.child("profile_pics").child(uri.lastPathSegment!!)

        var uploadTask = mReference.putFile(uri)

        try {
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                println("Upload is $progress% done")
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

        try {
            uploadTask.addOnSuccessListener { taskSnapshot: TaskSnapshot ->
                val url: String = taskSnapshot.metadata?.reference?.downloadUrl.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                Log.v("Upload Image", url)
                setResult(Activity.RESULT_OK, resultIntent)
                super.finish()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }


    }


    @Throws(IOException::class)
    open fun createImageFile(): File? {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        imageFilePath = image.absolutePath
        Log.v("CREATE IMAGE FILE", "saved to " + imageFilePath)
        return image
    }

    private fun getImageFromPhone() {
        val pickIntent = Intent()
        pickIntent.type = "image/*"
        pickIntent.action = Intent.ACTION_GET_CONTENT
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val pickTitle = "Select or take a new Picture"
        val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
        )
        startActivityForResult(chooserIntent, CODE_IMG_GALLERY)
    }

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
//                    ...
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "${BuildConfig.APPLICATION_ID}.provider",
                        it
                    )
                    Log.v("DISPATCH_FUNC", "after getting photoURI= " + photoURI.toString())
                    // add intents for files and camera
                    val pickIntent = Intent()
                    pickIntent.type = "image/*"
                    pickIntent.action = Intent.ACTION_GET_CONTENT

                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE, photoURI)
                    val pickTitle = "Select or take a new Picture"
//                    val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
                    takePictureIntent.putExtra(
                        Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
                    )
                    // fin
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    super.startActivityForResult(takePictureIntent, CODE_IMG_GALLERY)
                }
            }
        }
    }

}

