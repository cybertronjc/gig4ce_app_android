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
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.verification.gigerVerfication.bankDetails.AddBankDetailsInfoFragment
import com.gigforce.app.modules.preferences.PreferencesFragment
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.StringConstants
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
    private var mode: Int = EnrollmentConstants.MODE_UNSPECIFIED
    private var cameFromEnrollment = false

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

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_profile_picture, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
        getProfilePictureForUser()
    }

    private fun getProfilePictureForUser() {
        if (mode == EnrollmentConstants.MODE_ADD) {
            //dont fetch doc

            profile_pic_Uploading.gone()
            submitBtn.visible()
            skipButton.gone()
            submitBtn.text = "Upload Photo"
            editLayout.gone()
        } else {
            val isRequirementMode = mode != EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT
            submitBtn.text = if (isRequirementMode) "Change Photo" else "Next"
            skipButton.isVisible = isRequirementMode
            viewModel.getProfileForUser(userId)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            cameFromEnrollment = it.getBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, false)
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {
            cameFromEnrollment = it.getBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, false)
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
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
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
        outState.putBoolean(AddBankDetailsInfoFragment.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT, cameFromEnrollment)

    }

    private fun initListeners() {

        imageView13.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }

        editLayout.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }

        submitBtn.setOnClickListener {

            if (userId != null) {


                if (submitBtn.text == "Next") {
                    navigate(
                            R.id.addUserInterestFragment, bundleOf(
                            EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                            EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                            EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                            EnrollmentConstants.INTENT_EXTRA_MODE to mode
                    ))
                } else {
                    checkForPermissionElseShowCameraGalleryBottomSheet()
                }
            } else {

                if (mode == EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT&&submitBtn.text=="Next") {
                    popBackState()
                } else {
                    checkForPermissionElseShowCameraGalleryBottomSheet()
                }
            }
        }

        ic_back_btn.setOnClickListener {
            if (userId == null) {
                if (cameFromEnrollment) {
                    onBackPressed()
                    return@setOnClickListener
                }
                activity?.onBackPressed()
            } else {
                showGoBackConfirmationDialog()
            }
        }

        skipButton.setOnClickListener {

            navigate(
                    R.id.addUserInterestFragment, bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                    EnrollmentConstants.INTENT_EXTRA_MODE to mode)
            )
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            ClickOrSelectImageBottomSheet.launch(childFragmentManager, false,this)
        else
            requestStoragePermission()
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
        viewModel.profile
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    when (it) {
                        Lce.Loading -> {
                            profile_pic_Uploading.visible()
                        }
                        is Lce.Content -> {
                            profile_pic_Uploading.gone()

                            submitBtn.visible()
                            if (it.content.hasUserUploadedProfilePicture()) {
                                displayImage(it.content.profileAvatarName)
                                editLayout.visible()
                            } else {
                                skipButton.gone()
                                editLayout.gone()
                                submitBtn.text = "Upload Photo"
                            }
                        }
                        is Lce.Error -> {
                            profile_pic_Uploading.gone()
                            showToast(it.error)
                        }
                    }
                })

        viewModel.submitUserDetailsState
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    it ?: return@Observer

                    when (it) {
                        Lse.Loading -> {
                            profile_pic_Uploading.visible()
                        }
                        Lse.Success -> {
                            profile_pic_Uploading.gone()
                            viewModel.getProfileForUser(userId)

//                            if (userId == null) {
//                                //Normal User login
//                                submitBtn.text = "Back"
//                            } else {
                                submitBtn.text = "Next"
//                            }

                            showToast(getString(R.string.profile_pic_uploaded))
                        }
                        is Lse.Error -> {
                            profile_pic_Uploading.gone()
                            showAlertDialog(getString(R.string.could_not_submit_info), it.error)
                        }
                    }
                })


    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {

            val profilePicRef: StorageReference = PreferencesFragment
                    .storage
                    .reference
                    .child("profile_pics")
                    .child(profileImg)

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
                    ClickOrSelectImageBottomSheet.launch(childFragmentManager, false,this)
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

    override fun removeProfilePic() {

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
            val fvImage = imageUriResultCrop?.let { FirebaseVisionImage.fromFilePath(requireContext(), it) }

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
        if (cameFromEnrollment) {
            navFragmentsData?.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            popBackState()
            return true
        }
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
        if (cameFromEnrollment) {
            onBackPressed()
            return
        }
        findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }


    companion object {

        private const val REQUEST_CAPTURE_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

}