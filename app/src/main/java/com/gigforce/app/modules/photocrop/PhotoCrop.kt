package com.gigforce.app.modules.photocrop

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.modules.photoCrop.ui.main.ProfilePictureOptionsBottomSheetFragment.BottomSheetListener
import com.gigforce.app.modules.profile.ProfileViewModel
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
    BottomSheetListener {
    private val CODE_IMG_GALLERY: Int = 1
    private val EXTENSION: String = ".jpg"
    private val DEFAULT_PICTURE: String = "avatar.jpg"
    private var cropX: Float = 1F
    private var cropY: Float = 1F
    private val resultIntent: Intent = Intent()
    private var PREFIX: String = "IMG"
    private var detectFace: Int = 1;
    private lateinit var storage: FirebaseStorage
    private lateinit var storageDirPath: String
    private lateinit var CLOUD_PICTURE_FOLDER: String
    private lateinit var incomingFile: String
    private lateinit var imageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var viewModel: ProfileViewModel
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

    /**
     * Every call to start crop needs to have an extra with the key "purpose"
     * based on the purpose, a function can be created to get the other extras
     * depending on the purpose. Should not initiate other bundles in onCreate
     * to keep this activity adaptable to all cropping purposes
     */
    override fun onCreate(savedInstanceState: Bundle?): Unit {
        super.onCreate(savedInstanceState)

        this.setContentView(R.layout.activity_photo_crop)
        storage = FirebaseStorage.getInstance()
        imageView = this.findViewById(R.id.profile_avatar_photo_crop)
        backButton = this.findViewById(R.id.back_button_photo_crop)
        var purpose: String = intent.getStringExtra("purpose")
        Log.e("PHOTO_CROP", "purpose = " + purpose + " comparing with: profilePictureCrop")
        if (purpose == "profilePictureCrop") profilePictureOptions()
    }

    private fun profilePictureOptions() {
        Log.e("PHOTO_CROP", "profile picture options started")
        CLOUD_PICTURE_FOLDER = intent.getStringExtra("folder")
        incomingFile = intent.getStringExtra("file")
        cropX = 1F
        cropY = 1F
        PREFIX = intent.getStringExtra("uid")
        val bundle = intent.extras
        if (bundle != null) {
            storageDirPath = bundle.get("fbDir").toString()
            detectFace = bundle.get("detectFace") as Int
        }
        loadImage(incomingFile)
        showBottomSheet()
    }


    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        backButton.setOnClickListener {
            super.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        super.finish()

    }

    override fun onRestart() {
        super.onRestart()
        showBottomSheet()

    }

    /**
     * Logic for what happens when the bottom sheet clickables are used
     */
    override fun onButtonClicked(id: Int) {
        when (id) {
            R.id.updateProfilePicture -> getImageFromPhone()
            R.id.removeProfilePicture -> defaultProfilePicture()
        }

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ): Unit {
        super.onActivityResult(requestCode, resultCode, data)
        var bundle = data?.extras
        if (null != bundle) logBundle(bundle)

        /**
         * Gets uri when the image is to be captured from gallery or camera
         */
        if (requestCode == CODE_IMG_GALLERY && resultCode == Activity.RESULT_OK) {
            var imageUri: Uri? = data?.data
            if (imageUri == null) {
                imageUri = getImageUriFromBitmap(
                    this.applicationContext,
                    data?.extras!!.get("data") as Bitmap
                )
            }
            Log.v("IMAGE_CAPTURE", "request code="+requestCode.toString()+"  ImURI: "+imageUri.toString())
            if (imageUri != null) {
                startCrop(imageUri)
            }
        }

        /**
         * Handles data which is a resultant from cropping activity
         */
        else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            Log.d("ImageUri", imageUriResultCrop.toString())

            var baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                var bitmap = data?.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            var fvImage = imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(this, it) }

            //  Face detect - Check if face is present in the cropped image or not.
            if (detectFace === 1) {
                val result = detector.detectInImage(fvImage!!)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        if (faces.size > 0) {
                            Toast.makeText(
                                this,
                                "Face detected, successfully updated your profile pic" + faces[0].boundingBox.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            upload(imageUriResultCrop, baos.toByteArray())

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
                        upload(imageUriResultCrop, baos.toByteArray())
                    }
            } else {
                //just upload wihtout face detection eg for pan, aadhar, other docs.
                upload(imageUriResultCrop, baos.toByteArray())

            }
        }
        Log.d("CStatus", "completed result on activity")
    }

    /**
     * To get uri from the data received when using the camera to capture image
     */
    open fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path =
            MediaStore.Images.Media.insertImage(context.contentResolver, bitmap, "Title", null)
        return Uri.parse(path.toString())
    }

    /**
     * Generate a unique name of the file to be uploaded using time stamp
     * Initiates Crop activity
     */
    private fun startCrop(uri: Uri): Unit {
        Log.v("Start Crop", "started")
        //can use this for a new name every time
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = PREFIX + "_" + timeStamp + "_"
        val uCrop: UCrop = UCrop.of(
            uri,
            Uri.fromFile(File(cacheDir, imageFileName + EXTENSION))
        )
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        uCrop.withAspectRatio(cropX, cropY)
        uCrop.withMaxResultSize(450, 450)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this as AppCompatActivity)

    }

    /**
     * Settings for the ucrop activity need to be changed here.
     */
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

        /**
         * Uploading task created and initiated here.
         */
        lateinit var uploadTask: UploadTask
        uploadTask = if (uri != null) {
            Log.d("UPLOAD", "uploading files")
            mReference.putFile(uri)
        } else {
            Log.d("UPLOAD", "uploading bytes")
            mReference.putBytes(data)
        }

        /**
         *  OnProgressListener to capture the progress. Can be used to create an upload progress bar
         */
        try {
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                println("Uploading in progress :$progress% done")
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }

        /**
         *  OnSuccessListener to update profileAvatarName in viewModel
         *  Reload image preview.
         *  Change status of Activity Result
         */
        try {
            uploadTask.addOnSuccessListener { taskSnapshot: TaskSnapshot ->
                val name: String = taskSnapshot.metadata?.reference?.name.toString()
                viewModel.setProfileAvatarName(name)
                loadImage(name)
                Toast.makeText(this, "Successfully Uploaded :)", Toast.LENGTH_LONG).show()
                Log.v("Upload Image", name)
                setResult(Activity.RESULT_OK, resultIntent)
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * called when remove profile picture is called.
     * Changes the value of field profileAvatarName in the view model.
     * Reloads the Image preview
     */
    private fun defaultProfilePicture() {
        viewModel.setProfileAvatarName(DEFAULT_PICTURE)
        loadImage(DEFAULT_PICTURE)
        resultIntent.putExtra("filename", DEFAULT_PICTURE)
        setResult(Activity.RESULT_OK, resultIntent)
    }

    private fun loadImage(Path: String) {
        Log.d("PHOTO_CROP", "loading - " + Path)
        var profilePicRef: StorageReference =
            storage.reference.child(CLOUD_PICTURE_FOLDER).child(Path)
        GlideApp.with(this)
            .load(profilePicRef)
            .into(imageView)
    }

    /**
     * Creates the intent to use files and camera that will be cropped.
     * Chosen files are saven as temporary file with the name profilePicture.jpg
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

    /**
     * Needs to be called whenever the bottom sheet needs to be recreated.
     */
    private fun showBottomSheet() {
        var profilePictureOptionsBottomSheetFragment: ProfilePictureOptionsBottomSheetFragment =
            ProfilePictureOptionsBottomSheetFragment()
        profilePictureOptionsBottomSheetFragment.show(
            supportFragmentManager,
            "profilePictureOptionBottomSheet"
        )
    }

}