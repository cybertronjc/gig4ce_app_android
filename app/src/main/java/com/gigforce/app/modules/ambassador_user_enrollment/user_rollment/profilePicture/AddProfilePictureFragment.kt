package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.profilePicture

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
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
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
    }

    private fun initListeners() {

        imageView13.setOnClickListener {
            ClickOrSelectImageBottomSheet.launch(childFragmentManager, this)
        }

        submitBtn.setOnClickListener {

            if (userId != null) {
                navigate(
                    R.id.addCurrentAddressFragment, bundleOf(
                        EnrollmentConstants.INTENT_EXTRA_USER_ID to userId
                    )
                )
            } else {
                activity?.onBackPressed()
            }
        }

        ic_back_btn.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
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
                        showToast("Profile Picture uploaded")

                    }
                    is Lse.Error -> {
                        profile_pic_Uploading.gone()
                        showAlertDialog("Could not submit info", it.error)
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
                .into(imageView13)
        } else {
            GlideApp.with(this.requireContext())
                .load(R.drawable.avatar)
                .into(imageView13)
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
                showToast("Issue in capturing or selecting Image")
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
                            "Face Detected. Uploading...",
                            Toast.LENGTH_LONG
                        ).show()
                        imageClickedOrSelectedNowUpload(imageUriResultCrop, baos.toByteArray())
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Something seems off. Please take a smart selfie with good lights.",
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
        options.setToolbarTitle("Crop and Rotate")
        return options
    }


    companion object {

        private const val REQUEST_CAPTURE_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"
    }

}