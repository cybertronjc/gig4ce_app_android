package com.gigforce.verification.mainverification.signature

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.FragmentSingatureCaptureFullScreenBinding
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignatureImageCaptureFragment :
    BaseFragment2<FragmentSingatureCaptureFullScreenBinding>(
        fragmentName = "SignatureImageCaptureFragment",
        layoutId = R.layout.fragment_singature_capture_full_screen,
        statusBarColor = R.color.lipstick_2
    ), ImageCropCallback {

    companion object {
        const val TAG = "FullScreenSignatureImageCaptureDialogFragment"
        const val INTENT_EXTRA_SIGNATURE_IMAGE_URL = "signature_image_url_path"
        private const val SHOULD_REMOVE_BACKGROUND_FROM_SIGNATURE = false
    }

    @Inject
    lateinit var navigation: INavigation

    private val viewModel: SignatureUploadViewModel by viewModels()
    private val sharedViewModel: SharedSignatureUploadViewModel by activityViewModels()

    private val cameraAndGalleryIntegrator: CameraAndGalleryIntegrator by lazy {
        CameraAndGalleryIntegrator(this)
    }

    @SuppressLint("NewApi")
    private val requestCameraPermissionContract = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { contactPermissionGranted ->

        if (contactPermissionGranted) {
            cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
        } else {
            val hasUserOptedForDoNotAskAgain =
                requireActivity().shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)
                    .not()
            if (hasUserOptedForDoNotAskAgain) {

                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Camera permission granted")
                    .setMessage("You will be redirected to setting page, please grant camera permission to capture signature image")
                    .setPositiveButton(getString(R.string.okay_common_ui)) { _, _ -> openSettingsPage() }
                    .setNegativeButton(getString(R.string.cancel_common_ui)) { _, _ -> }
                    .show()
            }
        }
    }

    private fun openSettingsPage() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun getImageCropOptions(): ImageCropOptions {

        return ImageCropOptions
            .Builder()
            .shouldOpenImageCrop(true)
            .setShouldEnableFaceDetector(false)
            .shouldEnableFreeCrop(true)
            .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var userId : String? = null
        arguments?.let {
            userId = it.getString(CommonIntentExtras.INTENT_USER_ID) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(CommonIntentExtras.INTENT_USER_ID) ?: return@let
        }

        if(userId != null) {
            viewModel.userId = userId!!
        }

        viewModel.checkForExistingSignature()
    }

    override fun viewCreated(
        viewBinding: FragmentSingatureCaptureFullScreenBinding,
        savedInstanceState: Bundle?
    ) {
        initListeners()
        initViewModel()
    }

    private fun initListeners() = viewBinding.apply {

        this.appBar.setBackButtonListener{
            navigation.navigateUp()
        }

        this.clikImageBtn.setOnClickListener {

            if (cameraPermissionsGranted()) {
                cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
            } else {

                requestCameraPermissionContract.launch(
                    Manifest.permission.CAMERA
                )
            }
        }

        this.submitCancelBtn.setOnClickListener {
            viewModel.handleEvent(SignatureViewEvents.DoneOrCancelButtonClicked)
        }
    }

    private fun initViewModel() {
        viewModel.shouldRemoveBackgroundFromSignature = SHOULD_REMOVE_BACKGROUND_FROM_SIGNATURE

        viewModel.viewState
            .observe(viewLifecycleOwner, {

                when (it) {
                    SignatureUploadViewState.CheckingExistingSignature -> showCheckingPreviousImageLayoutRemovingLayout()
                    is SignatureUploadViewState.ErrorWhileCheckingExsitingSignature -> {
                        showCaptureImageLayout(true)
                    }
                    is SignatureUploadViewState.ShowExistingExistingSignature -> {

                        if (it.signatureUri != null){
                            showImageWithBackgroundRemoved(it.signatureUri,true)
                        } else{
                            showCaptureImageLayout(true)
                        }
                    }

                    SignatureUploadViewState.RemovingBackgroundFromSignature -> showBackgroundImageBackgroundRemovingLayout()
                    is SignatureUploadViewState.BackgroundRemovedFromSignature -> {
                        showImageWithBackgroundRemoved(it.processedImage,it.enableSubmitButton)
                    }
                    is SignatureUploadViewState.ErrorWhileRemovingBackgroundFromSignature -> showImageWithBackgroundRemoved(
                        it.processedImage,
                        true
                    )
                    is SignatureUploadViewState.ErrorUploadingSignatureImage -> {
                        viewBinding.previewScreen.uploadingAnimationView.gone()

                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage("Unable to upload image")
                            .setPositiveButton("Okay") { _, _ -> }
                            .show()
                    }
                    SignatureUploadViewState.NavigateBackToPreviousScreen -> navigation.navigateUp()
                    is SignatureUploadViewState.SignatureUploaded -> handleSignatureUploadCompleteOrOperationCancellation(
                        it.firebaseCompletePath,
                        it.firebaseImageFullUrl
                    )
                    SignatureUploadViewState.UploadingSignature -> showSignatureUploading()

                }
            })
    }

    private fun showSignatureUploading() {
        viewBinding.captureLayout.root.gone()
        viewBinding.previewScreen.root.visible()

        viewBinding.previewScreen.uploadingAnimationView.visible()
    }

    private fun showErrorWhileRemovingBackground(
        processedImage: Uri
    ) {
        viewBinding.captureLayout.removingBackgroundProgressBar.gone()
    }

    private fun showCheckingPreviousImageLayoutRemovingLayout() {
        showCaptureImageLayout(false)
        viewBinding.captureLayout.removingBackgroundProgressBar.visible()
    }

    private fun showBackgroundImageBackgroundRemovingLayout() {
        viewBinding.captureLayout.removingBackgroundProgressBar.visible()
    }

    private fun showCaptureImageLayout(
        enableButtons : Boolean
    ){
        viewBinding.captureLayout.removingBackgroundProgressBar.gone()
        viewBinding.captureLayout.root.gone()

        viewBinding.previewScreen.root.visible()
        viewBinding.previewScreen.signatureImage.clearImage()

        viewBinding.clikImageBtn.text = "Upload"
        viewBinding.submitCancelBtn.text = "Cancel"

        viewBinding.clikImageBtn.isEnabled = enableButtons
        viewBinding.submitCancelBtn.isEnabled = enableButtons
        viewBinding.submitCancelBtn.setStrokeColorResource(R.color.lipstick_2)
    }


    private fun showImageWithBackgroundRemoved(processedImage: Uri, enableSubmitButton: Boolean) {
        viewBinding.captureLayout.removingBackgroundProgressBar.gone()
        viewBinding.captureLayout.root.gone()

        viewBinding.previewScreen.root.visible()
        viewBinding.previewScreen.signatureImage.loadImage(processedImage)

        viewBinding.clikImageBtn.text = "Change"
        viewBinding.submitCancelBtn.text = "Done"

        if(enableSubmitButton){
            viewBinding.clikImageBtn.isEnabled = true
            viewBinding.submitCancelBtn.isEnabled = true
            viewBinding.submitCancelBtn.setStrokeColorResource(R.color.lipstick_2)
        } else{
            viewBinding.clikImageBtn.isEnabled = false
            viewBinding.submitCancelBtn.isEnabled = false
            viewBinding.submitCancelBtn.setStrokeColorResource(R.color.grey)
        }

    }

    private fun handleSignatureUploadCompleteOrOperationCancellation(
        signature: String,
        fullImageUrl: String
    ) {

        sharedViewModel.signatureCapturedAndUploaded(
            signature,
            fullImageUrl
        )

        navigation.navigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CameraAndGalleryIntegrator.REQUEST_CAPTURE_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_PICK_IMAGE,
            CameraAndGalleryIntegrator.REQUEST_CROP,
            ImageCropActivity.CROP_RESULT_CODE -> {

                if (resultCode == Activity.RESULT_OK) {

                    cameraAndGalleryIntegrator.parseResults(
                        requestCode,
                        resultCode,
                        data,
                        getImageCropOptions(),
                        this@SignatureImageCaptureFragment
                    )
                }
            }
        }
    }

    override fun errorWhileCapturingOrPickingImage(e: Exception) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Unable to capture image")
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    override fun imageResult(uri: Uri) {
        viewBinding.submitCancelBtn.isEnabled = true
        viewModel.handleEvent(SignatureViewEvents.SignatureCaptured(uri))
    }

    private fun cameraPermissionsGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

}