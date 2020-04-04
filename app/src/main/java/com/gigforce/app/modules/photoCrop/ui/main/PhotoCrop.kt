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
import com.gigforce.app.utils.GlideApp
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.yalantis.ucrop.UCrop
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class PhotoCrop : AppCompatActivity(),
    ProfilePictureOptionsBottomSheetFragment.BottomSheetListener {

    private val CODE_IMG_GALLERY: Int = 1
    private val REQUEST_TAKE_PHOTO: Int = 1
    private val EXTENSION: String = ".jpg"
    private val resultIntent: Intent = Intent()
    private lateinit var storage: FirebaseStorage
    private lateinit var storageDirPath:String;
    private var detectFace:Int = 1;
    private val DEFAULT_PICTURE: String = "avatar.jpg"
    private lateinit var CLOUD_PICTURE_FOLDER: String
    private lateinit var incomingFile: String
    private lateinit var imageView: ImageView

    var mStorage: FirebaseStorage = FirebaseStorage.getInstance()

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

    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        val bundle = intent.extras

        if (bundle != null) {
            storageDirPath = bundle.get("fbDir").toString()
            detectFace = bundle.get("detectFace") as Int
        }
        //getImageFromPhone()
        this.setContentView(R.layout.activity_photo_crop)
        CLOUD_PICTURE_FOLDER = intent.getStringExtra("folder")
        incomingFile = intent.getStringExtra("file")
        storage = FirebaseStorage.getInstance()
        imageView = this.findViewById(R.id.profile_avatar_photo_crop)
        showBottomSheet()
    }

    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference =
            storage.reference.child(CLOUD_PICTURE_FOLDER).child(Path)
        GlideApp.with(this)
            .load(profilePicRef)
            .into(imageView)
    }

    override fun onStart() {
        super.onStart()
        loadImage(incomingFile)
    }

    override fun onRestart() {
        super.onRestart()
        showBottomSheet()
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
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {

//            val imageUri: Uri? = getImageUri(this, data?.data)
            var imageUri: Uri? = data?.data
            if (imageUri == null) {
                imageUri = getImageUriFromBitmap(this.applicationContext, data?.extras!!.get("data") as Bitmap)
            }
            Log.v("COME IMG GALLERY", requestCode.toString())
            Log.v("ImURI", imageUri.toString())
            if (imageUri != null) {
                startCrop(imageUri)
            }
        } else if ((requestCode == UCrop.REQUEST_CROP || requestCode == REQUEST_TAKE_PHOTO) && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            Log.d("ImageUri", imageUriResultCrop.toString())
            print(requestCode)
            if (imageUriResultCrop != null) {
                Log.v("REQUEST CROP", requestCode.toString())
            }
            var baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                var bitmap = data?.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            var fvImage = imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(this, it) }

            //  Face detect - Check if face is present in the cropped image or not.
            if(detectFace===1) {
                val result = detector.detectInImage(fvImage!!)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        if (faces.size > 0) {
                            Toast.makeText(
                                this,
                                "Face detected, successfully updated your profile pic" + faces[0].boundingBox.toString(),
                                Toast.LENGTH_LONG
                            ).show();
                            upload(imageUriResultCrop, baos.toByteArray());
                        } else {
                            Toast.makeText(
                                this,
                                "No face detected, please re-upload another pic containing face",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d("CStatus", "Face detection failed! still uploading the image")
                        upload(imageUriResultCrop, baos.toByteArray());
                    }
            }
            else{
                //just upload wihtout face detection eg for pan, aadhar, other docs.
                upload(imageUriResultCrop, baos.toByteArray());
            }
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

        Log.v("Upload Image", "started")
        var mReference =
            mStorage.reference.child(storageDirPath).child(uri!!.lastPathSegment!!)

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

    private fun getImageFromPhone() {
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

    fun showBottomSheet() {
        var profilePictureOptionsBottomSheetFragment: ProfilePictureOptionsBottomSheetFragment =
            ProfilePictureOptionsBottomSheetFragment()
        profilePictureOptionsBottomSheetFragment.show(
            supportFragmentManager,
            "profilePictureOptionBottomSheet"
        )
    }

    override fun onButtonClicked(id: Int) {
        when (id) {
            R.id.updateProfilePicture -> getImageFromPhone()
            R.id.removeProfilePicture -> defaultProfilePicture()
        }
    }

    fun defaultProfilePicture() {
        resultIntent.putExtra("filename", DEFAULT_PICTURE)
        setResult(Activity.RESULT_OK, resultIntent)
        super.finish()
    }

    private fun logBundle(bundle: Bundle) {
        for (key in bundle.keySet()!!) {
            Log.e(
                "PHOTO_CROP_EXTRAS",
                key + " : " + if (bundle.get(key) != null) bundle.get(key) else "NULL"
            )
        }
    }

}