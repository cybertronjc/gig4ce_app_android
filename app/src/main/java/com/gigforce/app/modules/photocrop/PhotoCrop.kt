package com.gigforce.app.modules.photocrop

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.gigforce.app.R
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.UploadTask.TaskSnapshot
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.activity_photo_crop.*
import kotlinx.android.synthetic.main.profile_photo_bottom_sheet.*
import kotlinx.android.synthetic.main.profile_photo_bottom_sheet.view.*
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoCrop : AppCompatActivity() {

    companion object {
        var profilePictureOptionsBottomSheetFragment: ProfilePictureOptionsBottomSheetFragment =
            ProfilePictureOptionsBottomSheetFragment()


        const val INTENT_EXTRA_PURPOSE = "purpose"
        const val INTENT_EXTRA_FIREBASE_FOLDER_NAME = "fbDir"
        const val INTENT_EXTRA_FIREBASE_FILE_NAME = "file"
        const val INTENT_EXTRA_DETECT_FACE = "detectFace"

        const val PURPOSE_UPLOAD_PAN_IMAGE = "upload_pan_image"
        const val PURPOSE_UPLOAD_AADHAR_FRONT_IMAGE = "aadhar_front_image"
        const val PURPOSE_UPLOAD_AADHAR_BACK_IMAGE = "aadhar_back_image"
        const val PURPOSE_UPLOAD_BANK_DETAILS_IMAGE = "bank_passbook_image"
        const val PURPOSE_UPLOAD_DL_BACK_IMAGE = "dl_back_image"
        const val PURPOSE_UPLOAD_DL_FRONT_IMAGE = "dl_front_image"
    }

    private val CODE_IMG_GALLERY: Int = 1
    private val EXTENSION: String = ".jpg"
    private var cropX: Float = 1F
    private var cropY: Float = 1F
    private val resultIntent: Intent = Intent()
    private var PREFIX: String = "IMG"
    private lateinit var storage: FirebaseStorage
    private lateinit var CLOUD_INPUT_FOLDER: String
    private var detectFace: Int = 1
    private val DEFAULT_PICTURE: String = "avatar.jpg"
    private val profilePictureCrop: String = "profilePictureCrop"
    private val verification: String = "verification"
    private lateinit var CLOUD_OUTPUT_FOLDER: String
    private lateinit var incomingFile: String
    private lateinit var imageView: ImageView
    private lateinit var backButton: ImageButton
    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>
    private val TEMP_FILE: String = "profile_picture"
    private lateinit var purpose: String

    private val gigerVerificationViewModel: GigVerificationViewModel by viewModels()

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
        var constLayout: ConstraintLayout = this.findViewById(R.id.constraintLayout)
        var linearLayoutBottomSheet: LinearLayout = findViewById(R.id.linear_layout_bottomsheet)
        bottomSheetBehavior = BottomSheetBehavior.from(linearLayoutBottomSheet)
        purpose = intent.getStringExtra("purpose")
        Log.e("PHOTO_CROP", "purpose = " + purpose + " comparing with: profilePictureCrop")
        /**
         * Add new purpose call here.
         */
        when (purpose) {
            profilePictureCrop -> profilePictureOptions()
            verification -> verificationOptions()
            else -> verificationOptions()
        }
        checkPermissions()
        imageView.setOnClickListener { toggleBottomSheet() }
        constLayout.setOnClickListener { toggleBottomSheet() }
    }

    private fun profilePictureOptions() {
        Log.e("PHOTO_CROP", "profile picture options started")
        CLOUD_OUTPUT_FOLDER = intent.getStringExtra("folder")
        incomingFile = intent.getStringExtra("file")
        cropX = 1F
        cropY = 1F
        PREFIX = "profile_" + intent.getStringExtra("uid")
        val bundle = intent.extras
        if (bundle != null) {
            CLOUD_INPUT_FOLDER = bundle.get("fbDir").toString()
            detectFace = bundle.get("detectFace") as Int
            incomingFile = bundle.get("file").toString()
        }
        showBottomSheet()
        loadImage(CLOUD_INPUT_FOLDER, incomingFile)
    }


    private fun verificationOptions() {
        Log.e("PHOTO_CROP", "verification options started")
        CLOUD_OUTPUT_FOLDER = intent.getStringExtra("folder")
        incomingFile = "verification_" + intent.getStringExtra("file")
        cropX = 7F
        cropY = 5F
        PREFIX = intent.getStringExtra("uid") ?: viewModel.uid
        val bundle = intent.extras
        if (bundle != null) {
            CLOUD_INPUT_FOLDER = bundle.get("fbDir").toString()
            detectFace = bundle.get("detectFace") as Int
            CLOUD_OUTPUT_FOLDER = bundle.get("folder").toString()
            incomingFile = bundle.get("file").toString()
        }
        showBottomSheet()
        loadImage(CLOUD_INPUT_FOLDER, incomingFile)

        getImageFromPhone()
    }

    override fun onStart() {
        super.onStart()
        backButton.setOnClickListener {
            super.finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        super.finish()

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
            Log.v(
                "IMAGE_CAPTURE",
                "request code=" + requestCode.toString() + "  ImURI: " + imageUri.toString()
            )
            startCrop(imageUri)
        }

        /**
         * Handles data which is a resultant from cropping activity
         */
        else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            Log.d("ImageUri", imageUriResultCrop.toString())
            if (imageUriResultCrop != null) {
                resultIntent.putExtra("uri", imageUriResultCrop)
                Log.v("REQUEST CROP", requestCode.toString())
            }
            var baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                var bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            var fvImage = imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(this, it) }

            //  Face detect - Check if face is present in the cropped image or not.
            if (detectFace == 1) {
                val result = detector.detectInImage(fvImage!!)
                    .addOnSuccessListener { faces ->
                        // Task completed successfully
                        if (faces.size > 0) {
                            Toast.makeText(
                                this,
                                "Face Detected. Uploading...",
                                Toast.LENGTH_LONG
                            ).show()
                            upload(imageUriResultCrop, baos.toByteArray(), CLOUD_OUTPUT_FOLDER)

                        } else {
                            Toast.makeText(
                                this,
                                "Something seems off. Please take a smart selfie with good lights.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d("CStatus", "Face detection failed! still uploading the image")
                        upload(imageUriResultCrop, baos.toByteArray(), CLOUD_OUTPUT_FOLDER)
                    }
            } else {
                //just upload wihtout face detection eg for pan, aadhar, other docs.
                upload(imageUriResultCrop, baos.toByteArray(), CLOUD_OUTPUT_FOLDER)
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

    private fun upload(uri: Uri?, data: ByteArray, folder: String) {
        Log.v("Upload Image", "started")
        var mReference = mStorage.reference.child(folder).child(uri!!.lastPathSegment!!)

//            when (purpose) {
//            profilePictureCrop -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_PAN_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_BANK_DETAILS_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_AADHAR_FRONT_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_AADHAR_BACK_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_DL_FRONT_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            PURPOSE_UPLOAD_DL_BACK_IMAGE -> mStorage.reference.child("verification").child("${folder}/${uri!!.lastPathSegment!!}")
//            else ->
//        }

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
//        uploadTask.observe(.progress, handler: { (snap) in
//                print("Our upload progress is: \(String(describing: snap.progress?.fractionCompleted))")
//        })
        try {
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred) / taskSnapshot.totalByteCount
                println("Uploading in progress :$progress% done")
                progress_circular.visibility = View.VISIBLE
                progress_circular.progress = progress.toInt()
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
                progress_circular.visibility = View.GONE
                val fname: String = taskSnapshot.metadata?.reference?.name.toString()
                updateViewModel(purpose, fname)
                //loadImage(folder, fname)
                Toast.makeText(this, "Successfully Uploaded", Toast.LENGTH_LONG).show()
                Log.v(
                    "PHOTO_CROP",
                    "uploaded file in foldername" + CLOUD_OUTPUT_FOLDER + " file: " + fname
                )
                setResult(Activity.RESULT_OK, resultIntent)
                onBackPressed()
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show()
        }
    }

    /**
     * Contains any changes that need to be done to view model on successful upload
     *
     * @param purpose - to define the logic that should be followed in update
     * @param name - required for updating profile picture value ( alternate constructors can be made for different arguments )
     */
    private fun updateViewModel(purpose: String, name: String) {
        when (purpose) {
            profilePictureCrop -> viewModel.setProfileAvatarName(name)
            PURPOSE_UPLOAD_PAN_IMAGE -> gigerVerificationViewModel.updatePanImagePath(
                userhasPan = true,
                panPath = name
            )
            PURPOSE_UPLOAD_BANK_DETAILS_IMAGE -> gigerVerificationViewModel.updateBankPassbookImagePath(
                userHasPassBook = true,
                passbookImagePath = name
            )
            PURPOSE_UPLOAD_AADHAR_FRONT_IMAGE -> gigerVerificationViewModel.updateAadharData(
                true,
                name,
                null
            )
            PURPOSE_UPLOAD_AADHAR_BACK_IMAGE -> gigerVerificationViewModel.updateAadharData(
                true,
                null,
                name
            )
            PURPOSE_UPLOAD_DL_FRONT_IMAGE -> gigerVerificationViewModel.updateDLData(
                userHasDL = true,
                frontImagePath = name,
                backImagePath = null
            )
            PURPOSE_UPLOAD_DL_BACK_IMAGE -> gigerVerificationViewModel.updateDLData(
                userHasDL = true,
                frontImagePath = null,
                backImagePath = name
            )
        }
    }

    /**
     * called when remove profile picture is called.
     * Changes the value of field profileAvatarName in the view model.
     * Reloads the Image preview
     */
    private fun defaultProfilePicture() {
        if (purpose == profilePictureCrop) {
            viewModel.setProfileAvatarName(DEFAULT_PICTURE)
            loadImage(CLOUD_INPUT_FOLDER, DEFAULT_PICTURE)
            resultIntent.putExtra("filename", DEFAULT_PICTURE)
            setResult(Activity.RESULT_OK, resultIntent)
            onBackPressed()
        }
    }

    /**
     *
     */
    private fun loadImage(folder: String, path: String) {
        if (path == DEFAULT_PICTURE) disableRemoveProfilePicture()
        else enableRemoveProfilePicture()
        Log.d("PHOTO_CROP", "loading - " + path)
        var profilePicRef: StorageReference =
            storage.reference.child(folder).child(path)
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
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        galleryIntent.type = "image/gallery"
        val pickTitle = "Select or take a new Picture"
        var outputFileUri: Uri? = Uri.fromFile(File.createTempFile(TEMP_FILE, EXTENSION))
        val chooserIntent = Intent.createChooser(pickIntent, pickTitle)
        chooserIntent.putExtra(
            Intent.EXTRA_INITIAL_INTENTS,
            arrayOf(takePhotoIntent, galleryIntent)
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
     * Initialises the bottomsheet
     */
    private fun showBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        linear_layout_bottomsheet.updateProfilePicture.setOnClickListener { getImageFromPhone() }
        linear_layout_bottomsheet.removeProfilePicture.setOnClickListener { confirmRemoval() }
    }

    private fun toggleBottomSheet() {
        if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED) bottomSheetBehavior.state =
            BottomSheetBehavior.STATE_COLLAPSED
        else if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED) bottomSheetBehavior.state =
            BottomSheetBehavior.STATE_EXPANDED
    }

    private fun hasCameraPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.CAMERA)
    }

    private fun hasGalleryPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun askPermissions(rationale: String, requestCode: Int, perm: String) {
        EasyPermissions.requestPermissions(
            this,
            rationale,
            requestCode,
            perm
        )
    }

    private fun checkPermissions() {
        if (!hasCameraPermission()) askPermissions(
            "Camera Permission",
            101,
            Manifest.permission.CAMERA
        )
        if (!hasGalleryPermission()) askPermissions(
            "Select Image",
            102,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

    }

    private fun enableRemoveProfilePicture() {
        linear_layout_bottomsheet.removeProfilePicture.isClickable = true
        linear_layout_bottomsheet.removeProfilePicture.setTextColor(resources.getColorStateList(R.color.text_color))

    }

    private fun disableRemoveProfilePicture() {
        linear_layout_bottomsheet.removeProfilePicture.isClickable = false
        linear_layout_bottomsheet.removeProfilePicture.setTextColor(resources.getColor(R.color.lightGrey))
    }


    private fun confirmRemoval() {
        val dialog = this.let { Dialog(it) }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = dialog.findViewById(R.id.title) as TextView
        titleDialog.text = "Are sure you want to Remove the picture ?"
        val noBtn = dialog.findViewById(R.id.yes) as TextView
        noBtn.text = "No"
        val yesBtn = dialog.findViewById(R.id.cancel) as TextView
        yesBtn.text = "Yes"
        yesBtn.setOnClickListener()
        {
            defaultProfilePicture()
            dialog.dismiss()
        }
        noBtn.setOnClickListener()
        { dialog.dismiss() }
        dialog.show()
    }

}
