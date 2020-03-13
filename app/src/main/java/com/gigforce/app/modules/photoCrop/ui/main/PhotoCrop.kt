package com.gigforce.app.modules.photoCrop.ui.main

import android.Manifest
import android.R.attr
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.*


class PhotoCrop : AppCompatActivity() {

    private val CODE_IMG_GALLERY: Int = 1
    private val MY_PERMISSIONS_REQUEST_CAMERA: Int =1
    private val REQUEST_IMAGE_CAPTURE: Int = 1888
    private val SAMPLE_CROPPED_IMG_NAME: String = "SampleCropImg2"
    //lateinit var uri : Uri
    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //do your stuff
        }else {
            Log.d("Camera Perission","got here")
            ActivityCompat.requestPermissions(this,
                arrayOf({Manifest.permission.CAMERA}.toString()),
                MY_PERMISSIONS_REQUEST_CAMERA)
        }



//        startActivityForResult(
////            Intent().setAction(Intent.ACTION_GET_CONTENT)
////                .setType("image/*"), CODE_IMG_GALLERY

//
////    }
//    )

        val pickIntent = Intent()
        pickIntent.type = "image/*"
        pickIntent.action = Intent.ACTION_GET_CONTENT

        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        val pickTitle = "Select or take a new Picture"
        val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
        )

        startActivityForResult(chooserIntent, REQUEST_IMAGE_CAPTURE)

//        startActivityForResult(getPickImageChooserIntent(this,"title",true,true), CODE_IMG_GALLERY)

//        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
//            takePictureIntent.resolveActivity(packageManager)?.also {
//    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)}
//            startActivityForResult(Intent().setAction(takePictureIntent.REQUEST_IMAGE_CAPTURE).setType("image/*"), CODE_IMG_GALLERY)}
//
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === Activity.RESULT_OK) {
            if (requestCode === REQUEST_IMAGE_CAPTURE) {
                if (attr.data != null) {
                    val inputStream: InputStream?
                    var bitmap: Bitmap? = null
                    try {
                        if (attr.data != null) {
                            inputStream = contentResolver.openInputStream(data)
                            bitmap = BitmapFactory.decodeStream(inputStream)
                        } else {
                            bitmap = data.extras
                        }
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }
                }
            }
        }

        Log.v("MAYANK", requestCode.toString()+" RESULT:_"+resultCode.toString()+" UCrop.REQUEST_CROP "+UCrop.REQUEST_CROP.toString())
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            Log.v("COME IMG GALLERY", requestCode.toString())
            if (imageUri != null) {
                startCrop(imageUri)
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
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
        var destinationFileName: String? = SAMPLE_CROPPED_IMG_NAME
        destinationFileName += ".jpg"
        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(cacheDir, destinationFileName))
        )
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withAspectRatio(16F, 9F)
        uCrop.withMaxResultSize(450, 450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this as AppCompatActivity)

    }

    open fun getCropOptions(): UCrop.Options {
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

    private fun upload(uri:Uri) {
        var mReference = mStorage.reference.child("profile_pics").child(uri.lastPathSegment!!)
        try {
            mReference.putFile(uri).addOnSuccessListener { taskSnapshot: TaskSnapshot->
                val url:String = taskSnapshot.metadata?.reference?.downloadUrl.toString()
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                print(url)
            }
        }catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "IMG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )
        imageFilePath = image.absolutePath
        return image
    }

}