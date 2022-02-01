package com.gigforce.common_image_picker.image_capture_camerax.fragments

import android.graphics.*
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.gigforce.common_image_picker.ImageUtility
import com.gigforce.common_image_picker.R
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewModel
import com.gigforce.common_image_picker.image_capture_camerax.CaptureImageSharedViewState
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.io.*
import java.util.*
import kotlin.Exception


class ImageViewerFragment : Fragment() {

    private val sharedCameraViewModel: CaptureImageSharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(CaptureImageSharedViewModel::class.java)
    }

    private lateinit var imageView: ImageView
    private lateinit var discardImageBtn: View
    private lateinit var approveImageBtn: View
    private lateinit var rotateImageBtn : View
    private lateinit var progressBar: ProgressBar

    //Arguments
    private lateinit var image: File
    private var shouldUploadImageToo: Boolean = false
    private var parentDirectoryNameInFirebaseStorage: String? = null

    // High-accuracy landmark detection and face classification
    val highAccuracyOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
        .build()


    val detector = FaceDetection.getClient(highAccuracyOpts)

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
        rotateImageBtn = view.findViewById(R.id.rotate_img)
        progressBar = view.findViewById(R.id.progress_circular)

        discardImageBtn.setOnClickListener {
            sharedCameraViewModel.clickedImageDiscarded()
        }

        rotateImageBtn.setOnClickListener {
            rotateImageClockwise()
        }

        approveImageBtn.setOnClickListener {
            //detecting face after approved
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                detectFace()
            } else {
                sharedCameraViewModel.clickedImageApproved(
                    requireContext(),
                    shouldUploadImageToo,
                    image,
                    parentDirectoryNameInFirebaseStorage
                )
            }
        }
    }

    private fun rotateImageClockwise() {
        Glide.with(requireContext()).clear(imageView)

        image = ImageUtility.loadRotateAndSaveImage(
            requireContext(),
            image
        ) ?: return
        Glide.with(requireContext()).load(image).into(imageView)
    }


    fun detectFace() {

            var image1: InputImage? = null
            try {
                image1 = InputImage.fromFilePath(requireContext(), image.toUri())
                //  Face detect - Check if face is present in the image or not.
                val result = detector.process(image1)
                    .addOnSuccessListener { faces ->
                        if(!isAdded) return@addOnSuccessListener

                        // Task completed successfully
                        if (faces.size > 0) {
                            Log.d("FaceDetect", "success")
                            context?.getString(R.string.face_detected_common)?.let { showToast(it) }
                            sharedCameraViewModel.clickedImageApproved(
                                requireContext(),
                                shouldUploadImageToo,
                                image,
                                parentDirectoryNameInFirebaseStorage
                            )

                        } else {
                            Log.d("FaceDetect", "failed")
                            context?.let { showToast(it?.getString(R.string.something_seems_off_common)) }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Task failed with an exception
                        Log.d("FaceDetect", "failed ${e.message}")
                        sharedCameraViewModel.clickedImageApproved(
                            requireContext(),
                            shouldUploadImageToo,
                            image,
                            parentDirectoryNameInFirebaseStorage
                        )
                    }
            } catch (e: OutOfMemoryError){
                Log.d("FaceDetect", "failed (out of memory) ${e.message}")
                sharedCameraViewModel.clickedImageApproved(
                    requireContext(),
                    shouldUploadImageToo,
                    image,
                    parentDirectoryNameInFirebaseStorage
                )
            }

            catch (e: Exception) {
                sharedCameraViewModel.clickedImageApproved(
                    requireContext(),
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
        sharedCameraViewModel
            .captureImageSharedViewModelState
            .observe(requireActivity(), Observer {

                when (it) {
                    is CaptureImageSharedViewState.ImageUploadFailed -> {
                        if (!isAdded) return@Observer

                        progressBar.visibility = View.GONE
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.image_upload_failed_common))
                            .setMessage(getString(R.string.unable_to_upload_image_common) + it.error)
                            .setPositiveButton(getString(R.string.okay_common)) { _, _ -> }
                            .show()
                    }
                    is CaptureImageSharedViewState.ImageUploading -> {
                        if (!isAdded) return@Observer

                        progressBar.visibility = View.VISIBLE
                        progressBar.progress = it.progress
                    }
                    else -> {
                    }
                }
            })
    }

    fun showToast(
        text : String
    ){
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
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
