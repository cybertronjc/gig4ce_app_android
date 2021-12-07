package com.gigforce.common_ui.signature

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.R
import com.gigforce.common_ui.databinding.FragmentSingatureCaptureFullScreenBinding
import com.gigforce.common_ui.viewmodels.signature.SharedSignatureViewModel
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FullScreenSignatureImageCaptureDialogFragment : BaseFragment2<FragmentSingatureCaptureFullScreenBinding>(
    fragmentName = "FullScreenSignatureImageCaptureDialogFragment",
    layoutId = R.layout.fragment_singature_capture_full_screen,
    statusBarColor = R.color.lipstick_2
), ImageCropCallback {

    companion object {
        const val TAG = "FullScreenSignatureImageCaptureDialogFragment"
        private const val SHOULD_REMOVE_BACKGROUND_FROM_SIGNATURE = false
    }

    @Inject lateinit var navigation : INavigation

    private val viewModel : SignatureUploadViewModel by viewModels()
    private val sharedViewModel : SharedSignatureViewModel by activityViewModels()

    private val cameraAndGalleryIntegrator: CameraAndGalleryIntegrator by lazy {
        CameraAndGalleryIntegrator(this)
    }

    private fun getImageCropOptions() : ImageCropOptions {

        return ImageCropOptions
            .Builder()
            .shouldOpenImageCrop(true)
            .setShouldEnableFaceDetector(false)
            .shouldEnableFreeCrop(true)
            .build()
    }


    override fun viewCreated(
        viewBinding: FragmentSingatureCaptureFullScreenBinding,
        savedInstanceState: Bundle?
    ) {
        initListeners()
        initViewModel()
    }

    private fun initListeners() = viewBinding.apply {

        this.submitBtn.setOnClickListener {
            cameraAndGalleryIntegrator.showCameraAndGalleryBottomSheet()
        }
    }

    private fun initViewModel() {
       viewModel.shouldRemoveBackgroundFromSignature = SHOULD_REMOVE_BACKGROUND_FROM_SIGNATURE

        viewModel.viewState
            .observe(viewLifecycleOwner,{

                when(it){
                    is SignatureUploadViewState.BackgroundRemovedFromSignature -> TODO()
                    is SignatureUploadViewState.ErrorUploadingSignatureImage -> TODO()
                    is SignatureUploadViewState.ErrorWhileRemovingBackgroundFromSignature -> TODO()
                    SignatureUploadViewState.RemovingBackgroundFromSignature -> TODO()
                    is SignatureUploadViewState.SignatureUploadCompletedOrCancelled -> handleSignatureUploadCompleteOrOperationCancellation(
                        it.signature
                    )
                    is SignatureUploadViewState.SignatureUploaded -> TODO()
                    SignatureUploadViewState.UploadingSignature -> TODO()
                }
            })
    }

    private fun handleSignatureUploadCompleteOrOperationCancellation(
        signature: String?
    ) {

        if(signature != null){
            sharedViewModel.signatureUploaded(signature)
        }

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
                        this@FullScreenSignatureImageCaptureDialogFragment
                    )
                }

            }
        }
    }

    override fun errorWhileCapturingOrPickingImage(e: Exception) {
        MaterialAlertDialogBuilder(requireContext())
            .setMessage("Unable to capture image")
            .setPositiveButton("Okay"){_,_ -> }
            .show()
    }

    override fun imageResult(uri: Uri) {
       viewModel.handleEvent(SignatureViewEvents.SignatureCaptured(uri))
    }
}