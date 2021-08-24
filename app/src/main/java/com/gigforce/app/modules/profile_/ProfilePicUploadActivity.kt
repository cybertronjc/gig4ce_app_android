package com.gigforce.app.modules.profile_

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.gigforce.ambassador.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.R
import com.gigforce.common_image_picker.ClickOrSelectImageBottomSheet
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.layout_profile_pic_upload_activity.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ProfilePicUploadActivity : AppCompatActivity(),
    ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {
    private var isPicturePresent: Boolean = false
    private val viewModel: ProfileViewModel by viewModels()
    private val viewModelUser: UserDetailsViewModel by viewModels()

    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"

    private val REQUEST_CAPTURE_IMAGE = 101
    private val REQUEST_PICK_IMAGE = 102
    private val PREFIX: String = "IMG"
    private val EXTENSION: String = ".jpg"
    private val REQUEST_STORAGE_PERMISSION = 102
    private val options = with(FirebaseVisionFaceDetectorOptions.Builder()) {
        setModeType(FirebaseVisionFaceDetectorOptions.ACCURATE_MODE)
        setLandmarkType(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
        setClassificationType(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
        setMinFaceSize(0.15f)
        setTrackingEnabled(true)
        build()
    }
    private val detector = FirebaseVision.getInstance()
        .getVisionFaceDetector(options)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_profile_pic_upload_activity)
        initClicks()
        initObservers()
    }


    private fun initObservers() {
        viewModel.getProfileData().observe(this, androidx.lifecycle.Observer {
            viewModel.profileID = it?.id ?: ""
            loadImage(it.profileAvatarName)
        })
        viewModelUser.submitUserDetailsState
            .observe(this, androidx.lifecycle.Observer {
                it ?: return@Observer

                when (it) {
                    Lse.Loading -> {
                        progress_bar_upload.visible()
                    }
                    Lse.Success -> {
                        progress_bar_upload.gone()
                        viewModelUser.getProfileForUser(viewModel.profileID)
                        Toast.makeText(
                            this,
                            getString(R.string.profile_pic_uploaded_app),
                            Toast.LENGTH_LONG
                        ).show()

                    }
                    is Lse.Error -> {
                        progress_bar_upload.gone()
                        showAlertDialog(getString(R.string.could_not_submit_info_app), it.error)
                    }
                }
            })
    }

    private fun loadImage(path: String) {
        isPicturePresent = path != "avatar.jpg" && path != ""
        iv_no_profile_pic_profile_pic_upload.isVisible = !isPicturePresent
        iv_profile_pic_full_profile_upload.isVisible = isPicturePresent
        val profilePicRef: StorageReference =
            FirebaseStorage.getInstance().reference.child(PROFILE_PICTURE_FOLDER).child(path)
        GlideApp.with(this)
            .load(profilePicRef)
            .into(iv_profile_pic_full_profile_upload)
    }


    private fun initClicks() {
        tv_upload_picture_profile_pic_upload.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }
        iv_back_profile_pic_upload.setOnClickListener { onBackPressed() }
    }


    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(this)
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(this)
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    override fun removeProfilePic() {
        confirmRemoval()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(this, resultCode, data)
            if (outputFileUri != null) {
                startCrop(outputFileUri)
            } else {
                Toast.makeText(this, getString(R.string.issue_in_cap_image_app), Toast.LENGTH_LONG)
                    .show()
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput((data!!))
            Log.d("ImageUri", imageUriResultCrop.toString())

            val baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                val bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            }
            val fvImage =
                imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(this, it) }

            //  Face detect - Check if face is present in the cropped image or not.
            val result = detector.detectInImage(fvImage!!)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    if (faces.size > 0) {
                        Toast.makeText(
                            this,
                            getString(R.string.face_detected_upload_app),
                            Toast.LENGTH_LONG
                        ).show()
                        imageClickedOrSelectedNowUpload(imageUriResultCrop, baos.toByteArray())
                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.something_seems_of_app),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Toast.makeText(
                        this,
                        getString(R.string.no_face_app),
                        Toast.LENGTH_LONG
                    ).show()
                }

        }
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
            Uri.fromFile(File(this.cacheDir, imageFileName + EXTENSION))
        )
        val resultIntent: Intent = Intent()
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(this)
    }

    private fun getCropOptions(): UCrop.Options {
        val options: UCrop.Options = UCrop.Options()
        options.setCompressionQuality(70)
        options.setCompressionFormat(Bitmap.CompressFormat.PNG)
//        options.setMaxBitmapSize(1000)
        options.setHideBottomControls((false))
        options.setFreeStyleCropEnabled(false)
        options.setStatusBarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarColor(ResourcesCompat.getColor(resources, R.color.topBarDark, null))
        options.setToolbarTitle(getString(R.string.crop_and_rotate_app))
        return options
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            ClickOrSelectImageBottomSheet.launch(supportFragmentManager, isPicturePresent, this)
        else
            requestStoragePermission()
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ),
            REQUEST_STORAGE_PERMISSION
        )
    }

    private fun imageClickedOrSelectedNowUpload(uri: Uri?, data: ByteArray) {

        viewModelUser.uploadProfilePicture(
            viewModel.userProfileData.value?.enrolledBy?.id,
            uri,
            data
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
            .show()
    }

    private fun confirmRemoval() {
        val dialog = this.let { Dialog(it) }
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirmation_custom_alert_type1)
        val titleDialog = dialog.findViewById(R.id.title) as TextView
        titleDialog.text = getString(R.string.sure_to_remove)
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

    private val resultIntent: Intent = Intent()
    private val DEFAULT_PICTURE: String = "avatar.jpg"
    private fun defaultProfilePicture() {
        viewModel.setProfileAvatarName(DEFAULT_PICTURE)
        resultIntent.putExtra("filename", DEFAULT_PICTURE)
        setResult(Activity.RESULT_OK, resultIntent)
        onBackPressed()

    }

}