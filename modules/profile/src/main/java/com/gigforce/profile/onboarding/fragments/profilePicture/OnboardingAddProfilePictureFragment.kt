package com.gigforce.profile.onboarding.fragments.profilePicture

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_ui.shimmer.ShimmerHelper
import com.gigforce.core.FragmentHelper
import com.gigforce.core.IEventTracker
import com.gigforce.core.ProfilePropArgs
import com.gigforce.core.TrackingEventArgs
import com.gigforce.core.crashlytics.CrashlyticsLogger
import com.gigforce.core.date.DateHelper
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.GlideApp
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.gigforce.profile.R
import com.gigforce.profile.analytics.OnboardingEvents
import com.gigforce.profile.models.OnboardingProfileData
import com.gigforce.profile.onboarding.OnFragmentFormCompletionListener
import com.gigforce.profile.onboarding.OnboardingFragmentNew
import com.gigforce.profile.viewmodel.OnboardingViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_onboarding_profile_picture.*
import java.io.File
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingAddProfilePictureFragment() : Fragment(), ImageCropCallback, OnboardingFragmentNew.FragmentSetLastStateListener,OnboardingFragmentNew.FragmentInteractionListener,OnboardingFragmentNew.SetInterfaceListener {

    companion object {

        private const val REQUEST_CAPTURE_IMAGE = 101
        private const val REQUEST_PICK_IMAGE = 102

        private const val PREFIX: String = "IMG"
        private const val EXTENSION: String = ".jpg"

        private const val REQUEST_STORAGE_PERMISSION = 102

        fun newInstance() = OnboardingAddProfilePictureFragment()

    }

    private var viewShownFirstTime = false
    private val viewModel: OnboardingViewModel by viewModels()
    private var onboardingProfileData : OnboardingProfileData? = null

    @Inject
    lateinit var eventTracker: IEventTracker
    private val firebaseStorage: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    private val cameraAndGalleryIntegrator: CameraAndGalleryIntegrator by lazy {
        CameraAndGalleryIntegrator(this).apply {
            openFrontCamera()
        }
    }

    private val imageCropOptions: ImageCropOptions
        get() {

            val newFileName = "Onboarding-profile-${DateHelper.getFullDateTimeStamp()}.png"
            val imageFile = File(requireContext().filesDir, newFileName)

            return ImageCropOptions
                    .Builder()
                    .shouldOpenImageCrop(true)
                    .setShouldEnableFaceDetector(true)
                    .setOutputFileUri(imageFile.toUri())
                    .build()
        }

    fun hasUserUploadedPhoto() : Boolean {
       return onboardingProfileData?.hasUserUploadedProfilePicture() ?: false
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_onboarding_profile_picture, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initViewModel()
        getProfilePictureForUser()
    }

    private fun getProfilePictureForUser() {
        viewModel.getProfileForUser()
    }

    fun showCameraSheetIfNotShown(){

//        if (!viewShownFirstTime) {
            checkForPermissionElseShowCameraGalleryBottomSheet()
//            viewShownFirstTime = true
//        }
    }

    private fun initListeners() {

        imageView13.setOnClickListener {
            checkForPermissionElseShowCameraGalleryBottomSheet()
        }

        editLayout.setOnClickListener {

            if(onboardingProfileData?.hasUserUploadedProfilePicture() == true){
                showCameraSheetIfNotShown()
            } else {
                formCompletionListener?.profilePictureSkipPressed()
            }
        }
//        editLayout.setOnClickListener {
//            checkForPermissionElseShowCameraGalleryBottomSheet()
//        }

    }


    private fun checkForPermissionElseShowCameraGalleryBottomSheet() {
        if (hasStoragePermissions())
            cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
        else
            requestStoragePermission()
    }


    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay".capitalize()) { _, _ -> }
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
                            onboardingProfileData = it.content

                            shimmerFrameLayout.stopShimmer()
                            shimmerFrameLayout.gone()
                            imageView13.visible()

                            if (it.content.hasUserUploadedProfilePicture()) {
                                displayImage(it.content.profileAvatarName)
                               // formCompletionListener.changeTextButton("Upload Photo")
                                skip_edit_textview.text = "Change"
                            } else {
                                skip_edit_textview.text = "Skip"

                            }

                            formCompletionListener?.checkForButtonText()
                        }
                        is Lce.Error -> {
                            shimmerFrameLayout.stopShimmer()
                            shimmerFrameLayout.gone()
                            imageView13.visible()

                            Toast.makeText(requireContext(), it.error, Toast.LENGTH_SHORT).show()
                        }
                    }
                })

        viewModel.submitUserDetailsState
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                    it ?: return@Observer

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


                            viewModel.getProfileForUser()

                            Log.d("profile_picture", it.content)

                            eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_UPLOADED_PROFILE_PHOTO,null))
                            eventTracker.setProfileProperty(ProfilePropArgs("\$avatar",it.content))
                            Toast.makeText(
                                    requireContext(),
                                    "Profile Pic uploaded",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
                        is Lce.Error -> {
                            shimmerFrameLayout.stopShimmer()
                            shimmerFrameLayout.gone()
                            imageView13.visible()

                            showAlertDialog("could not submit info", it.error)
                        }
                    }
                })
    }

    private fun displayImage(profileImg: String) {
        if (profileImg != "avatar.jpg" && profileImg != "") {

            val profilePicRef: StorageReference = firebaseStorage
                    .reference
                    .child("profile_pics")
                    .child(profileImg)

            GlideApp.with(this.requireContext())
                    .load(profilePicRef)
                    .placeholder(ShimmerHelper.getShimmerDrawable())
                    .into(imageView13)
        } else {
            GlideApp.with(this.requireContext())
                    .load(R.drawable.ic_profile_avatar_pink)
                    .into(imageView13)
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
                    cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
                else {
                    Toast.makeText(
                            requireContext(),
                            "Please Grant storage permission",
                            Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            CameraAndGalleryIntegrator.REQUEST_CAPTURE_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_PICK_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_CROP -> {

                if (resultCode == Activity.RESULT_OK) {

                    cameraAndGalleryIntegrator.parseResults(
                            requestCode,
                            resultCode,
                            data,
                            imageCropOptions,
                            this@OnboardingAddProfilePictureFragment
                    )
                }
            }
        }
    }

    //
    //-----------------------------
    // Camera , Gallery and Image crop callbacks
    //-------------------------

    override fun errorWhileCapturingOrPickingImage(e: Exception) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Unable to click Photo, ${e.message}")
                .setPositiveButton("Okay") { _, _ -> }
                .show()

        CrashlyticsLogger.e("ProfilePicture", "WhileClickingProfilePicture", e)
    }

    override fun imageResult(uri: Uri) {
        viewModel.uploadProfilePicture(uri)
    }





    override fun lastStateFormFound(): Boolean {
        formCompletionListener?.enableDisableNextButton(true)
        return false
    }

    override fun nextButtonActionFound(): Boolean {
        var map = mapOf("OnboardingDone" to true)
        eventTracker.pushEvent(TrackingEventArgs(OnboardingEvents.EVENT_USER_COMPLETED_ONBOARDING,map))
        eventTracker.setUserProperty(map)
        return false
    }

    override fun activeNextButton() {
        formCompletionListener?.enableDisableNextButton(true)
    }

    var formCompletionListener: OnFragmentFormCompletionListener? = null
    override fun setInterface(onFragmentFormCompletionListener: OnFragmentFormCompletionListener) {
        formCompletionListener = formCompletionListener?:onFragmentFormCompletionListener
    }

}