package com.gigforce.common_ui.utils

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.gigforce.common_ui.R
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.common_ui.databinding.FragmentImageCropBinding
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.StringConstants
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.NavFragmentsData
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ImageCropFragment : Fragment(), IOnBackPressedOverride {

    companion object {
            const val logTag = "ImageCropFragment"
    }

    private lateinit var viewBinding: FragmentImageCropBinding
    private var cropImageUri: Uri? = null
    private lateinit var incomingFile: String

    @Inject
    lateinit var navigation: INavigation

    @Inject
    lateinit var logger: GigforceLogger

    val imageUriCompleteListener: CropImageView.OnSetImageUriCompleteListener? = null
    val cropImageCompleteListener: CropImageView.OnCropImageCompleteListener? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentImageCropBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        setIncomingImage(incomingFile)
        initListeners()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            incomingFile =
                it.getString(StringConstants.IMAGE_CROP_URI.value, "")

        } ?: run {
            arguments?.let {
                incomingFile =
                    it.getString(StringConstants.IMAGE_CROP_URI.value, "")
            }
        }
        logDataReceivedFromBundles()
    }

    private fun logDataReceivedFromBundles() {
        if (::incomingFile.isInitialized) {
            logger.d(logTag, "incomingFile received from bundles : $incomingFile")
        } else {
            logger.e(
                logTag,
                "no incomingFile-id received from bundles",
                Exception("no incomingFile-id received from bundles")
            )
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.IMAGE_CROP_URI.value, incomingFile)
    }



    private fun initListeners() = viewBinding.apply{

        //this will rotate the image by 90 degree in anticlockwise
        rotateImg.setOnClickListener {
            rotateImageAntiClock()
        }

        closeImg.setOnClickListener {
            //finish()
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Alert")
                .setMessage("Are you sure you want to close?")
                .setPositiveButton("Okay") { dialog, _ ->
                    dialog.dismiss()
                    activity?.onBackPressed()
                }
                .show()
        }

        okayImg.setOnClickListener {
            viewBinding.progressCircular.visibility = View.VISIBLE
            cropImageView.getCroppedImageAsync()

        }
        cropImageView.setOnCropImageCompleteListener(object : CropImageView.OnCropImageCompleteListener {
            override fun onCropImageComplete(
                view: CropImageView,
                result: CropImageView.CropResult
            ) {
                viewBinding.progressCircular.visibility = View.GONE
                handleCropResult(result)
            }

        })

        appBar.apply {
            setBackButtonListener(View.OnClickListener {
               activity?.onBackPressed()
            })
        }
    }

//    override fun onPause() {
//        super.onPause()
//        viewBinding.cropImageView.let {
//            it.setOnSetImageUriCompleteListener(null)
//            it.setOnCropImageCompleteListener(null)
//        }
//    }

    override fun onDetach() {
        super.onDetach()
        viewBinding.cropImageView.let {
            it.setOnSetImageUriCompleteListener(null)
            it.setOnCropImageCompleteListener(null)
        }

    }



    private fun setIncomingImage(incomingFile: String) = viewBinding.apply{
        if (incomingFile.isNotEmpty()){
            val incomingUri: Uri = Uri.parse(incomingFile)
            logger.d(logTag, "incomingFile uri : $incomingUri")
            cropImageView.setImageUriAsync(incomingUri)
            setCropImageControls()
        } else {
            showToast("Invalid incoming image")
        }

    }

    private fun rotateImageAntiClock(){
        viewBinding.cropImageView.rotateImage(90)
    }

    private fun setCropImageControls(){
        viewBinding.cropImageView.apply {
            setAspectRatio(1, 1)
            setCenterMoveEnabled(true)
            isShowProgressBar = true
            setBackgroundColor(resources.getColor(R.color.warm_grey))
            setFixedAspectRatio(true)
            setMultiTouchEnabled(false)
            isShowCropOverlay = true
        }
    }

//    override fun onCropImageComplete(view: CropImageView, result: CropImageView.CropResult) {
//        viewBinding.progressCircular.visibility = View.GONE
//        handleCropResult(result)
//    }

    private fun handleCropResult(result: CropImageView.CropResult) {
        if (result.isSuccessful && result.error == null) {
//            val imageBitmap =
//                if (viewBinding.cropImageView.cropShape == CropImageView.CropShape.OVAL)
//                    result.bitmap?.let { CropImage.toOvalBitmap(it) }

//            val imageBitmap =  result.bitmap
//            viewBinding.cropImageView.setImageBitmap(imageBitmap)
            Log.v("File Path", context?.let { result.getUriFilePath(it).toString() })
            Log.v("File result", "result : ${result.bitmap.toString()} , ${result.cropRect.toString()}, ${result.uriContent.toString()} , :bimap , ${result.error}" )

            logger.d(logTag, "cropped result  : ${result.uriContent.toString() }")
            //SCropResultActivity.start(this, imageBitmap, result.uriContent, result.sampleSize)
            var navFragmentsData = activity as NavFragmentsData
            navFragmentsData.setData(
                bundleOf(
                    StringConstants.IMAGE_CROP_URI.value to result.uriContent.toString()
                )
            )

        } else {
            Log.e("AIC", "Failed to crop image", result?.error)
            showToast("Crop failed: ${result?.error?.message}")

        }
    }

    override fun onBackPressed(): Boolean {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Alert")
            .setMessage("Are you sure you want to close?")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
                navigation.popBackStack()
            }
            .show()
        return true
    }

//    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
//        if (error != null) {
//            Log.e("AIC", "Failed to load image by URI", error)
//            Toast.makeText(activity, "Image load failed: " + error.message, Toast.LENGTH_LONG)
//                .show()
//        }
//    }




}