package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.profilePicture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.ImagePicker
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.GlideApp
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import com.google.firebase.storage.StorageReference
import com.yalantis.ucrop.UCrop
import kotlinx.android.synthetic.main.fragment_ambsd_profile_picture.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AddProfilePictureFragment : BaseFragment(),
    ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener {

    private val viewModel: UserDetailsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var userId: String? = null
    private var userName: String = ""
    private var pincode = ""

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_profile_picture, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_NAME, userName)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE, pincode)
    }

    private fun initListeners() {

        imageView13.setOnClickListener {

            if (hasStoragePermissions())
                ClickOrSelectImageBottomSheet.launch(childFragmentManager, this)
            else
                requestStoragePermission()

        }

        submitBtn.setOnClickListener {

            if (userId != null) {
                navigate(
                    R.id.addUserInterestFragment, bundleOf(
                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                        EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                        EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode
                    )
                )
            } else {
                activity?.onBackPressed()
            }
        }

        ic_back_btn.setOnClickListener {
            if (userId == null) {
                activity?.onBackPressed()
            } else {
                showGoBackConfirmationDialog()
            }
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
            requireContext(),
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

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay).capitalize()) { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lse.Loading -> {
                        profile_pic_Uploading.visible()
                    }
                    Lse.Success -> {
                        profile_pic_Uploading.gone()
                        showToast(getString(R.string.profile_pic_uploaded))

                    }
                    is Lse.Error -> {
                        profile_pic_Uploading.gone()
                        showAlertDialog(getString(R.string.could_not_submit_info), it.error)
                    }
                }
            })
        viewModel.profile
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                displayImage(it.profileAvatarName)
            })

        viewModel.startWatchingProfile(userId)
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {
            val profilePicRef: StorageReference =
                PreferencesFragment.storage.reference.child("profile_pics").child(profileImg)
            GlideApp.with(this.requireContext())
                .load(profilePicRef)
                .placeholder(getCircularProgressDrawable())
                .into(imageView13)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .into(imageView13)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_STORAGE_PERMISSION -> {

                var allPermsGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermsGranted = false
                        break
                    }
                }

                if (allPermsGranted)
                    ClickOrSelectImageBottomSheet.launch(childFragmentManager, this)
                else {
                    showToast(getString(R.string.please_grant_storage_permission))
                }
            }
        }
    }

    override fun onClickPictureThroughCameraClicked() {
        val intents = ImagePicker.getCaptureImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_CAPTURE_IMAGE)
    }

    override fun onPickImageThroughCameraClicked() {
        val intents = ImagePicker.getPickImageIntentsOnly(requireContext())
        startActivityForResult(intents, REQUEST_PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CAPTURE_IMAGE || requestCode == REQUEST_PICK_IMAGE) {
            val outputFileUri = ImagePicker.getImageFromResult(requireContext(), resultCode, data)
            if (outputFileUri != null) {
                startCrop(outputFileUri)
            } else {
                showToast(getString(R.string.issue_in_cap_image))
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
                imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(requireContext(), it) }

            //  Face detect - Check if face is present in the cropped image or not.
            val result = detector.detectInImage(fvImage!!)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    if (faces.size > 0) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.face_detected_upload),
                            Toast.LENGTH_LONG
                        ).show()
                        imageClickedOrSelectedNowUpload(imageUriResultCrop, baos.toByteArray())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_seems_of),
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Task failed with an exception
                    Log.d("CStatus", "Face detection failed! still uploading the image")
                    imageClickedOrSelectedNowUpload(imageUriResultCrop, baos.toByteArray())
                }

        }
    }

    private fun imageClickedOrSelectedNowUpload(uri: Uri?, data: ByteArray) {
        viewModel.uploadProfilePicture(userId, uri, data)
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
            Uri.fromFile(File(requireContext().cacheDir, imageFileName + EXTENSION))
        )
        val resultIntent: Intent = Intent()
        resultIntent.putExtra("filename", imageFileName + EXTENSION)
        uCrop.withAspectRatio(1F, 1F)
        uCrop.withMaxResultSize(1920, 1080)
        uCrop.withOptions(getCropOptions())
        uCrop.start(requireContext(), this)
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate))
        return options
    }

    override fun onBackPressed(): Boolean {

        return if (userId == null) {
            false
        } else {
            showGoBackConfirmationDialog()
            true
        }
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert))
            .setMessage(getString(R.string.are_u_sure_u_want_to_go_back))
            .setPositiveButton(getString(R.string.yes)) { _, _ -> goBackToUsersList() }
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .show()
    }

    private fun goBackToUsersList() {
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }


    companion object {

        private const val REQUEST_CAPTURE_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val actionCamera = 0
        private const val actionGallery = 1

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

}