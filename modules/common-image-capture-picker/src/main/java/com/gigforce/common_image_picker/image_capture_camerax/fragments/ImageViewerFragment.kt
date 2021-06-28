package com.gigforce.common_image_picker.image_capture_camerax.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewModel
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File


class ImageViewerFragment : Fragment() {

    private val sharedCameraViewModel: CaptureImageSharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(CaptureImageSharedViewModel::class.java)
    }

    private lateinit var imageView: ImageView
    private lateinit var discardImageBtn: View
    private lateinit var approveImageBtn: View
    private lateinit var progressBar : ProgressBar

    //Arguments
    private lateinit var image: File
    private var shouldUploadImageToo: Boolean = false
    private  var parentDirectoryNameInFirebaseStorage: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(
        R.layout.fragment_image_viewer,
        container,
        false
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findViews(view)
        getArgumentsFrom(arguments, savedInstanceState)
        Glide.with(requireContext()).load(image).into(imageView)
        initViewModel()
    }


    private fun findViews(view: View) {
        imageView = view.findViewById(R.id.show_pic)
        discardImageBtn = view.findViewById(R.id.retake_image)
        approveImageBtn = view.findViewById(R.id.upload_img)
        progressBar = view.findViewById(R.id.progress_circular)

        discardImageBtn.setOnClickListener {
            sharedCameraViewModel.clickedImageDiscarded()
        }

        approveImageBtn.setOnClickListener {

            sharedCameraViewModel.clickedImageApproved(
                shouldUploadImageToo,
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
            parentDirectoryNameInFirebaseStorage = it.getString(INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE)
        }

        savedInstanceState?.let {

            val imagePath = it.getString(INTENT_EXTRA_FILE_PATH) ?: return@let
            image = File(imagePath)

            shouldUploadImageToo = it.getBoolean(INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO)
            parentDirectoryNameInFirebaseStorage = it.getString(INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_FILE_PATH, image.absolutePath)
        outState.putBoolean(INTENT_EXTRA_SHOULD_UPLOAD_IMAGE_TOO, shouldUploadImageToo)
        outState.putString(INTENT_EXTRA_PARENT_DIRECTORY_NAME_IN_FIREBASE_STORAGE, parentDirectoryNameInFirebaseStorage)
    }

    private fun initViewModel() {
        sharedCameraViewModel
            .captureImageSharedViewModelState
            .observe(requireActivity(), Observer {

                when (it) {
                    is CaptureImageSharedViewState.ImageUploadFailed -> {
                        if(!isAdded) return@Observer

                        progressBar.visibility = View.GONE
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Image Upload failed")
                            .setMessage("Unable to upload image , ${it.error}")
                            .setPositiveButton("Okay") {_,_ ->}
                            .show()
                    }
                    is CaptureImageSharedViewState.ImageUploading -> {
                        if(!isAdded) return@Observer

                        progressBar.visibility = View.VISIBLE
                        progressBar.progress = it.progress
                    }
                    else -> {
                    }
                }
            })
    }



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
}
