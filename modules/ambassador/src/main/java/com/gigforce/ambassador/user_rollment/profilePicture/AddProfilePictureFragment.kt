package com.gigforce.ambassador.user_rollment.profilePicture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.ambassador.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.ext.getCircularProgressDrawable
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewmodels.ProfileViewModel
import com.gigforce.common_ui.widgets.ImagePicker
import com.gigforce.core.AppConstants
import com.gigforce.core.ScopedStorageConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.core.utils.NavFragmentsData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.yalantis.ucrop.UCrop
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_profile_picture.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class AddProfilePictureFragment : Fragment(),
    ClickOrSelectImageBottomSheet.OnPickOrCaptureImageClickListener, IOnBackPressedOverride {

    private val viewModel: UserDetailsViewModel by viewModels()
    private val profileViewModel: ProfileViewModel by viewModels()
    private var userId: String? = null
    private var userName: String = ""
    private var pincode = ""
    private var mode: Int = EnrollmentConstants.MODE_UNSPECIFIED
    private var cameFromEnrollment = false
    private var cameFromOnboarding = false
    private var navFragmentsData: NavFragmentsData? = null

    @Inject
    lateinit var navigation: INavigation

    private val currentUser: FirebaseUser by lazy {
        FirebaseAuth.getInstance().currentUser!!
    }

    private val options = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()


    private val detector = FaceDetection.getClient(options)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_profile_picture, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navFragmentsData = activity as NavFragmentsData
        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
        getProfilePictureForUser()
    }

    private fun getProfilePictureForUser() {

        if (mode != EnrollmentConstants.MODE_EDIT) {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }

        if (mode == EnrollmentConstants.MODE_ADD) {
            //dont fetch doc

            shimmerFrameLayout.gone()
            submitBtn.visible()
            skipButton.gone()
            submitBtn.text = getString(R.string.upload_photo_amb)
            editLayout.gone()
        } else if (cameFromOnboarding){
            skipButton.gone()
            submitBtn.text = getString(R.string.done_amb)
            viewModel.getProfileForUser(userId)
        } else {
            val isRequirementMode = mode != EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT
            submitBtn.text =
                if (isRequirementMode) getString(R.string.change_photo_amb) else getString(R.string.next_amb)
            skipButton.isVisible = isRequirementMode
            viewModel.getProfileForUser(userId)
        }
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            cameFromEnrollment = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
                false
            )
            cameFromOnboarding = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_ONBOARDING_FORM,
                false
            )
            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            userName = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_NAME) ?: return@let
            pincode = it.getString(EnrollmentConstants.INTENT_EXTRA_PIN_CODE) ?: return@let
        }

        savedInstanceState?.let {
            cameFromEnrollment = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
                false
            )
            cameFromOnboarding = it.getBoolean(
                AppConstants.INTENT_EXTRA_USER_CAME_FROM_ONBOARDING_FORM,
                false
            )
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
        outState.putBoolean(
            AppConstants.INTENT_EXTRA_USER_CAME_FROM_AMBASSADOR_ENROLLMENT,
            cameFromEnrollment
        )
        outState.putBoolean(AppConstants.INTENT_EXTRA_USER_CAME_FROM_ONBOARDING_FORM, cameFromOnboarding)

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
                    navigation.navigateTo(
                        "userinfo/addUserInterestFragment", bundleOf(
                            EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                            EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                            EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                            EnrollmentConstants.INTENT_EXTRA_MODE to mode
                        )
                    )
//                    navigate(
//                        R.id.addUserInterestFragment, bundleOf(
//                            EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                            EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                            EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                            EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                        )
//                    )
                } else if (submitBtn.text == "Done"){
                    navigation.popBackStack()
                }
                else {
                    checkForPermissionElseShowCameraGalleryBottomSheet()
                }
            } else {

                if (mode == EnrollmentConstants.MODE_ENROLLMENT_REQUIREMENT && submitBtn.text == "Next") {
                    navigation.popBackStack()
                } else {
                    checkForPermissionElseShowCameraGalleryBottomSheet()
                }
            }
        }

        toolbar_layout.apply {
            showTitle(getString(R.string.upload_profile_picture_amb))
            hideActionMenu()
            setBackButtonListener(View.OnClickListener {
                if (userId == null) {
                    if (cameFromEnrollment) {
                        onBackPressed()
                        return@OnClickListener
                    }
                    activity?.onBackPressed()
                } else {
                    showGoBackConfirmationDialog()
                }
            })
        }

        skipButton.setOnClickListener {
            navigation.navigateTo(
                "userinfo/addUserInterestFragment", bundleOf(
                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                )
            )
//            navigate(
//                R.id.addUserInterestFragment, bundleOf(
//                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                    EnrollmentConstants.INTENT_EXTRA_USER_NAME to userName,
//                    EnrollmentConstants.INTENT_EXTRA_PIN_CODE to pincode,
//                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                )
//            )
        }
    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            ClickOrSelectImageBottomSheet.launch(childFragmentManager, false, this)
        else
            requestStoragePermission()
    }

    private fun hasStoragePermissions(): Boolean {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        } else {

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
    }

    private fun requestStoragePermission() {

        if (Build.VERSION.SDK_INT >= ScopedStorageConstants.SCOPED_STORAGE_IMPLEMENT_FROM_SDK) {

            requestPermissions(
                arrayOf(
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        } else {

            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
                ),
                REQUEST_STORAGE_PERMISSION
            )
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay_amb).capitalize()) { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.profile
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it) {
                    Lce.Loading -> {
                        imageView13.gone()
                        shimmerFrameLayout.visible()
                        shimmerFrameLayout.startShimmer()
                    }
                    is Lce.Content -> {

                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.gone()
                        imageView13.visible()

                        submitBtn.visible()
                        if (it.content.hasUserUploadedProfilePicture()) {
                            displayImage(it.content.profileAvatarName)
                            editLayout.visible()
                        } else {
                            skipButton.gone()
                            editLayout.gone()
                            submitBtn.text = getString(R.string.upload_photo_amb)
                        }
                    }
                    is Lce.Error -> {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.gone()
                        imageView13.visible()

                        showToast(it.error)
                    }
                }
            })

        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                it ?: return@Observer

                when (it) {
                    Lse.Loading -> {

                        imageView13.gone()
                        shimmerFrameLayout.visible()
                        shimmerFrameLayout.startShimmer()
                    }
                    Lse.Success -> {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.gone()
                        imageView13.visible()

                        viewModel.getProfileForUser(userId)

//                            if (userId == null) {
//                                //Normal User login
//                                submitBtn.text = "Back"
//                            } else {
                        submitBtn.text = if(cameFromOnboarding) getString(R.string.done_amb) else getString(R.string.next_amb)
//                            }

                        showToast(getString(R.string.profile_pic_uploaded_amb))
                    }
                    is Lse.Error -> {
                        shimmerFrameLayout.stopShimmer()
                        shimmerFrameLayout.gone()
                        imageView13.visible()

                        showAlertDialog(getString(R.string.could_not_submit_info_amb), it.error)
                    }
                }
            })


    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {

            val profilePicRef: StorageReference = FirebaseStorage.getInstance()
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
                    ClickOrSelectImageBottomSheet.launch(childFragmentManager, false, this)
                else {
                    showToast(getString(R.string.please_grant_storage_permission_amb))
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
                showToast(getString(R.string.issue_in_cap_image_amb))
            }
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            val imageUriResultCrop: Uri? = UCrop.getOutput(data!!)
            Log.d("ImageUri", imageUriResultCrop.toString())

            val baos = ByteArrayOutputStream()
            if (imageUriResultCrop == null) {
                val bitmap = data.data as Bitmap
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            }
            val fvImage =
                imageUriResultCrop?.let { InputImage.fromFilePath(requireContext(), it) }

            //  Face detect - Check if face is present in the cropped image or not.
            val result = detector.process(fvImage!!)
                .addOnSuccessListener { faces ->
                    // Task completed successfully
                    if (faces.size > 0) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.face_detected_upload_amb),
                            Toast.LENGTH_LONG
                        ).show()
                        imageClickedOrSelectedNowUpload(imageUriResultCrop, baos.toByteArray())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.something_seems_of_amb),
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
        viewModel.uploadProfilePicture(userId, uri, data, cameFromOnboarding)
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
        options.setToolbarTitle(getString(R.string.crop_and_rotate_amb))
        return options
    }

    override fun onBackPressed(): Boolean {
        if (cameFromEnrollment) {
            navFragmentsData?.setData(bundleOf(StringConstants.BACK_PRESSED.value to true))
            navigation.popBackStack()
            return true
        }
        return if (userId == null) {
            false
        }else if (cameFromOnboarding){
            false
        }
        else {
            showGoBackConfirmationDialog()
            true
        }
    }

    private fun showGoBackConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.alert_amb))
            .setMessage(getString(R.string.are_u_sure_u_want_to_go_back_amb))
            .setPositiveButton(getString(R.string.yes_amb)) { _, _ -> goBackToUsersList() }
            .setNegativeButton(getString(R.string.no_amb)) { _, _ -> }
            .show()
    }

    private fun goBackToUsersList() {
        if (cameFromEnrollment) {
            onBackPressed()
            return
        }

        findNavController().navigateUp()
        //findNavController().popBackStack(R.id.ambassadorEnrolledUsersListFragment, false)
    }


    companion object {

        private const val REQUEST_CAPTURE_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102
    }

}