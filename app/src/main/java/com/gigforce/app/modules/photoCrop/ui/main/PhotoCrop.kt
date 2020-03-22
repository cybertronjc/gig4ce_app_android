package com.gigforce.app.modules.photoCrop.ui.main

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.gigforce.app.BuildConfig
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.theartofdev.edmodo.cropper.CropImage.getPickImageChooserIntent
import com.yalantis.ucrop.UCrop
import io.grpc.ManagedChannelProvider.provider
import io.grpc.ServerProvider.provider
import java.io.File
import java.io.IOException
import java.nio.channels.spi.AsynchronousChannelProvider.provider
import java.nio.channels.spi.SelectorProvider.provider
import java.text.SimpleDateFormat
import java.util.*


class PhotoCrop : AppCompatActivity() {

    lateinit var imageFilePath: String
    private val CODE_IMG_GALLERY: Int = 1
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int = 1
    private val REQUEST_IMAGE_CAPTURE: Int = 1888
    private val SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg2"
    private val REQUEST_TAKE_PHOTO: Int = 1

    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

//        val pickIntent = Intent()
//        pickIntent.type = "image/*"
//        pickIntent.action = Intent.ACTION_GET_CONTENT
//        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        val pickTitle = "Select or take a new Picture"
//        val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
//        chooserIntent.putExtra(
//            Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
//        )
//        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)

//        dispatchTakePictureIntent()

        startActivityForResult(getPickImageChooserIntent(this,"title",true,true), CODE_IMG_GALLERY)

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
                    Log.v("DISPATCH_FUNC","after getting photoURI= "+photoURI.toString())
                    // add intents for files and camera
                    val pickIntent = Intent()
                    pickIntent.type = "image/*"
                    pickIntent.action = Intent.ACTION_GET_CONTENT

                    val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE,photoURI)
                    val pickTitle = "Select or take a new Picture"
//                    val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
                    takePictureIntent.putExtra(
                            Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
                    )
                    // fin
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)

//        if (resultCode === Activity.RESULT_OK) {
//            if (requestCode === REQUEST_IMAGE_CAPTURE) {
////                if(pictureIntent.resolveActivity(getPackageManager()) != null){
//                //Create a file to store the image
//                var photoFile: File? = null
//                try {
//                    photoFile = createImageFile()
//                    if(photoFile != null) Log.v("AFTER CREATE IMAGE FILE", "photoFILE " + photoFile.toString())
//
//                    if (photoFile != null) {
//                        var photoUri: Uri =
//                            FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", photoFile)
//                        Log.v("AFTER FILE URI", "photoUri " + photoUri.toString())
//                        startCrop(photoUri)
//                    }
//
//                } catch (ex: IOException) {
//                    // Error occurred while creating the File
//                }
//
//            }
//        }


        Log.v(
                "MAYANK",
                requestCode.toString() + " RESULT:_" + resultCode.toString() + " UCrop.REQUEST_CROP " + UCrop.REQUEST_CROP.toString()+" data= "+data.toString()
        )
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            Log.v("COME IMG GALLERY", requestCode.toString())
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
                super.finish()
            }
        }
    }

    open fun startCrop(uri: Uri): Unit {

        Log.v("Start Crop", "started")
        var destinationFileName: String? = SAMPLE_CROPPED_IMG_NAME
        destinationFileName += ".jpg"

        val timeStamp = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir =
                getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val uCrop: UCrop = UCrop.of(
                uri,
//            Uri.fromFile(File(cacheDir, destinationFileName))
                Uri.fromFile(File.createTempFile(
                        imageFileName,  /* prefix */
                        ".jpg",  /* suffix */
                        storageDir /* directory */
                ))
        )
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withAspectRatio(16F, 9F)
        uCrop.withMaxResultSize(450, 450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this as AppCompatActivity)

    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.JPEG)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(true)
        options.setStatusBarColor(resources.getColor(R.color.colorPrimaryDark))
        options.setToolbarColor(resources.getColor(R.color.colorPrimary))
        options.setToolbarTitle("image adjustment")
        return options
    }

    private fun upload(uri: Uri) {
        var mReference =
                mStorage.reference.child("profile_pics").child(uri.lastPathSegment!!)
        try {
            mReference.putFile(uri).addOnSuccessListener { taskSnapshot: TaskSnapshot ->
                val url: String = taskSnapshot.metadata?.reference?.downloadUrl.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                print(url)
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

}

