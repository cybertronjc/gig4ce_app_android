package com.gigforce.app.modules.photoCrop.ui.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gigforce.app.R
import com.gigforce.app.modules.photoCrop.ui.main.ProfilePictureOptionsBottomSheetFragment.BottomSheetListener
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PhotoCrop : AppCompatActivity(), BottomSheetListener {

    companion object {
        fun newInstance() = PhotoCrop()
    }

    private val CODE_IMG_GALLERY: Int = 1
    private val REQUEST_TAKE_PHOTO: Int = 1
    private val EXTENSION: String = ".jpg"
    private val resultIntent: Intent = Intent()
    private lateinit var storage: FirebaseStorage
    private lateinit var CLOUD_PICTURE_FOLDER: String
    private lateinit var incomingFile:String
    private lateinit var imageView: ImageView
    private val DEFAULT_PICTURE: String = "avatar.jpg"


    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_photo_crop)
        CLOUD_PICTURE_FOLDER = intent.getStringExtra("folder")
        incomingFile = intent.getStringExtra("file")
        storage = FirebaseStorage.getInstance()
        imageView = this.findViewById(R.id.profile_avatar_photo_crop)
        showBottomSheet()
    }

    override fun onRestart() {
        super.onRestart()
        showBottomSheet()
    }

    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference =storage.reference.child(CLOUD_PICTURE_FOLDER).child(Path)
        GlideApp.with(this)
            .load(profilePicRef)
            .into(imageView)
    }

    override fun onStart(){
        super.onStart()
        loadImage(incomingFile)
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
            logBundle(bundle)
        }

        /*
        Handles when the image is chosen from gallery or returned from camera
         */
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
            var imageUri: Uri? = data?.data
            if (imageUri == null) {
                imageUri = getImageUriFromBitmap(this.applicationContext, data?.extras!!.get("data") as Bitmap)
            }
            Log.v("COME IMG GALLERY", requestCode.toString())
            Log.v("ImURI", imageUri.toString())
            if (imageUri != null) {
                startCrop(imageUri)
            }
        }

        /*
        Handles cropping the selected image
         */
        else if ((requestCode == UCrop.REQUEST_CROP || requestCode == REQUEST_TAKE_PHOTO) && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            Log.d("ImageUri", imageUriResultCrop.toString())
            if (imageUriResultCrop != null) {
                Log.v("REQUEST CROP", requestCode.toString())
            }
            var baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                var bitmap = data?.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            upload(imageUriResultCrop, baos.toByteArray())
        }

        Log.d("CStatus", "completed result on activity")
    }

    open fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    open fun startCrop(uri: Uri): Unit {
        Log.v("Start Crop", "started")
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

    private fun upload(uri: Uri?, data: ByteArray) {

        Log.v("UPLOAD", "started")
        var mReference =
                storage.reference.child("profile_pics").child(uri!!.lastPathSegment!!)

        lateinit var uploadTask: UploadTask
        if (uri != null) {
            Log.d("UPLOAD", "uploading files")
            uploadTask = mReference.putFile(uri)
        } else {
            Log.d("UPLOAD", "uploading bytes")
            uploadTask = mReference.putBytes(data)
        }


        try {
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                println("Uploading in progress :$progress% done")
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

    /*
    Creates the intent to use files and camera that will be cropped
     */

    open fun getImageFromPhone() {
        val pickIntent = Intent()
        pickIntent.type = "image/*"
        pickIntent.action = Intent.ACTION_GET_CONTENT
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val pickTitle = "Select or take a new Picture"
        var outputFileUri: Uri? = Uri.fromFile(File.createTempFile("profilePicture", ".jpg"))
        val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
        chooserIntent.putExtra(
                Intent.EXTRA_INITIAL_INTENTS, arrayOf(takePhotoIntent)
        )
        chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
        startActivityForResult(chooserIntent, CODE_IMG_GALLERY)
    }

    private fun logBundle(bundle: Bundle) {
        for (key in bundle.keySet()!!) {
            Log.e(
                    "PHOTO_CROP_EXTRAS",
                    key + " : " + if (bundle.get(key) != null) bundle.get(key) else "NULL"
            )
        }
    }

    open fun showBottomSheet(){
        var profilePictureOptionsBottomSheetFragment:ProfilePictureOptionsBottomSheetFragment=ProfilePictureOptionsBottomSheetFragment()
        profilePictureOptionsBottomSheetFragment.show(supportFragmentManager,"profilePictureOptionBottomSheet")
    }

    override fun onButtonClicked(id: Int) {
        when(id){
            R.id.selectProfilePicture -> getImageFromPhone()
            R.id.removeProfilePicture -> defaultProfilePicture()
        }
    }

    fun defaultProfilePicture() {
        resultIntent.putExtra("filename", DEFAULT_PICTURE)
        setResult(Activity.RESULT_OK, resultIntent)
        super.finish()
    }

}

