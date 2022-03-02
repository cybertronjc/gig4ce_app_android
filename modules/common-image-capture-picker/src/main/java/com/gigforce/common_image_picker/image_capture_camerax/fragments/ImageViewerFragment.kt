package com.gigforce.common_image_picker.image_capture_camerax.fragments

import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.ImageUtility
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewModel
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewState
import com.gigforce.common_image_picker.image_capture_camerax.ImageViewerViewModel
import com.gigforce.common_image_picker.image_capture_camerax.ImageViewerViewState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.toastfix.toastcompatwrapper.ToastHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.*
import java.util.*


class ImageViewerFragment : Fragment() {

    companion object {
        val TAG = ImageViewerFragment::class.java.simpleName

        private const val INTENT_EXTRA_FILE_PATH = "file_path"
        private const val INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO = "should_upload_image_too"
        private const val INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE =
            "parent_directory_name_in_storage"


        fun getInstance(
            file: File,
            shouldUploadImageToo: Boolean,
            parentDirectoryNameInFirebaseStorage: String?
        ): ImageViewerFragment {
            return ImageViewerFragment().apply {
                this.arguments = bundleOf(
                    INTENT_EXTRA_FILE_PATH to file.absolutePath,
                    INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO to shouldUploadImageToo,
                    INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE to parentDirectoryNameInFirebaseStorage
                )
            }
        }
    }

    private val viewModel: ImageViewerViewModel by lazy {
        ViewModelProvider(this).get(ImageViewerViewModel::class.java)
    }

    private val sharedCameraViewModel: CaptureImageSharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(CaptureImageSharedViewModel::class.java)
    }

    private lateinit var imageView: ImageView
    private lateinit var discardImageBtn: View
    private lateinit var approveImageBtn: View
    private lateinit var rotateImageBtn: View
    private lateinit var progressBar: ProgressBar

    //Arguments
    private lateinit var image: File
    private var shouldUploadImageToo: Boolean = false
    private var parentDirectoryNameInFirebaseStorage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.fragment_image_viewer,
        container,
        false
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getArgumentsFrom(arguments, savedInstanceState)
        viewModel.setSharedViewModel(sharedCameraViewModel)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        Glide.with(requireContext()).load(image).into(imageView)
        initViewModel()
    }


    private fun findViews(view: View) {
        imageView = view.findViewById(R.id.show_pic)
        discardImageBtn = view.findViewById(R.id.retake_image)
        approveImageBtn = view.findViewById(R.id.upload_img)
        rotateImageBtn = view.findViewById(R.id.rotate_img)
        progressBar = view.findViewById(R.id.progress_circular)

        discardImageBtn.setOnClickListener {
            sharedCameraViewModel.clickedImageDiscarded()
        }

        rotateImageBtn.setOnClickListener {

            viewModel.rotateImage(
                requireContext().applicationContext,
                image
            )
        }

        approveImageBtn.setOnClickListener {

            viewModel.detectFaceAndUploadImage(
                requireContext().applicationContext,
                image,
                parentDirectoryNameInFirebaseStorage
            )
        }
    }


    private fun getArgumentsFrom(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            val imagePath = it.getString(INTENT_EXTRA_FILE_PATH) ?: return@let
            image = File(imagePath)
            shouldUploadImageToo = it.getBoolean(INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO)
            parentDirectoryNameInFirebaseStorage =
                it.getString(INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE)
        }

        savedInstanceState?.let {

            val imagePath = it.getString(INTENT_EXTRA_FILE_PATH) ?: return@let
            image = File(imagePath)
            shouldUploadImageToo = it.getBoolean(INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO)
            parentDirectoryNameInFirebaseStorage =
                it.getString(INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_FILE_PATH, image.absolutePath)
        outState.putBoolean(INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO, shouldUploadImageToo)
        outState.putString(
            INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE,
            parentDirectoryNameInFirebaseStorage
        )
    }

    private fun initViewModel() {
        viewModel
            .viewState
            .observe(requireActivity(), Observer {
                if (!isAdded) return@Observer

                when (it) {
                    ImageViewerViewState.DetectingFace -> {
                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = true
                    }
                    ImageViewerViewState.FaceDetected -> {

                        ToastHandler.showToast(
                            requireContext(),
                            getString(R.string.face_detected_common),
                            Toast.LENGTH_SHORT
                        )
                    }
                    is ImageViewerViewState.ErrorWhileFaceDetection -> {

                        progressBar.visibility = View.GONE
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Unable to detect face")
                            .setMessage(getString(R.string.something_seems_off_common) )
                            .setPositiveButton(getString(R.string.okay_common)) { _, _ ->
                                sharedCameraViewModel.clickedImageDiscarded()
                            }
                            .show()
                    }
                    is ImageViewerViewState.ImageUploadFailed -> {

                        progressBar.visibility = View.GONE
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.image_upload_failed_common))
                            .setMessage(getString(R.string.unable_to_upload_image_common) + it.error)
                            .setPositiveButton(getString(R.string.okay_common)) { _, _ -> }
                            .show()
                    }
                    is ImageViewerViewState.ImageUploadSuccess -> {

                    }
                    is ImageViewerViewState.ImageUploading -> {

                        progressBar.visibility = View.VISIBLE
                        progressBar.isIndeterminate = false
                        progressBar.progress = it.progress
                    }
                    ImageViewerViewState.RotatingImage -> {
                        showToast("Rotating Image...")
                    }
                    is ImageViewerViewState.ImageRotated -> {

                        Glide.with(requireContext()).clear(imageView)

                        image = it.file
                        Glide.with(requireContext()).load(image).into(imageView)
                    }
                    is ImageViewerViewState.ImageRotationFailed -> {
                        showToast("Unable to rotate Image")
                    }
                    else -> {
                    }
                }
            })
    }

    fun showToast(
        text: String
    ) {
        ToastHandler.showToast(requireContext(), text, Toast.LENGTH_SHORT)
    }


}
