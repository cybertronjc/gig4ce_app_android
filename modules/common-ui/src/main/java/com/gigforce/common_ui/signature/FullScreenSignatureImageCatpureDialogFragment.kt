package com.gigforce.common_ui.signature

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.common_image_picker.CameraAndGalleryIntegrator
import com.gigforce.common_image_picker.ImageCropCallback
import com.gigforce.common_image_picker.ImageCropOptions
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.common_ui.R
import com.gigforce.common_ui.components.cells.AppBar
import com.gigforce.common_ui.views.GigforceImageView

class FullScreenSignatureImageCatpureDialogFragment : DialogFragment(), ImageCropCallback {

    companion object {
        const val TAG = "ViewFullScreenVideoDF"

        fun launch(
            childFragmentManager: FragmentManager,
            captureListener : FullScreenSignatureImageCaptureDialogFragmentListener
        ) {
            val frag = FullScreenSignatureImageCatpureDialogFragment().apply {
                this.setCaptureListener(captureListener)
            }
            frag.show(childFragmentManager, TAG)
        }
    }

    interface FullScreenSignatureImageCaptureDialogFragmentListener {

        fun onSignatureImageCaptured(
            uploadedSignatureImageUrl : String,
            signaturePathInFirebase : String
        )
    }

    private var listner : FullScreenSignatureImageCaptureDialogFragmentListener? = null

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



    private lateinit var appBar: AppBar
    private lateinit var imageView : GigforceImageView
    private lateinit var cameraButton : View
    private lateinit var galleryButton : View
    private lateinit var submitButton : View
    private lateinit var cancelButton : View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_singature_drawer_capture_full_screen, container, false)

    fun setCaptureListener(
        listner : FullScreenSignatureImageCaptureDialogFragmentListener
    ){
        this.listner = listner
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        initListners()
    }

    private fun initListners() {
        cameraButton.setOnClickListener {
            cameraAndGalleryIntegrator.startCameraForCapturing()
        }

        galleryButton.setOnClickListener {
            cameraAndGalleryIntegrator.startGalleryForPicking()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun findViews(view: View) {
        appBar = view.findViewById(R.id.appBar)
        imageView = view.findViewById(R.id.signature_image)
        cameraButton = view.findViewById(R.id.camera_btn)
        galleryButton = view.findViewById(R.id.gallery_btn)
        submitButton = view.findViewById(R.id.submit_btn)
        cancelButton = view.findViewById(R.id.cancel_btn)
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
                        this@FullScreenSignatureImageCatpureDialogFragment
                    )
                }

            }
        }
    }

    override fun errorWhileCapturingOrPickingImage(e: Exception) {

    }

    override fun imageResult(uri: Uri) {
        imageView.loadImage(uri)
    }
}