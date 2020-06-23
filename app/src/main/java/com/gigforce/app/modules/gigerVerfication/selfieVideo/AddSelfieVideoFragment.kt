package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.GigVerificationViewModel
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.PictureResult
import kotlinx.android.synthetic.main.fragment_add_selfie_video.*

class AddSelfieVideoFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_selfie_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }

        if (hasCameraPermissions())
            initCamera()
        else
            requestCameraPermission()

        selfieVideoCorrectCB.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked)
                enableSubmitButton()
            else
                disableSubmitButton()

        }
    }

    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() =
        requestPermissions(
            arrayOf(
                Manifest.permission.CAMERA
            ),
            REQUEST_CAMERA_PERMISSION
        )


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initCamera()
                }
            }
        }
    }

    private fun initCamera() {
        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(CameraListener())
    }

    private fun enableSubmitButton() {
        selfieVideoSubmitSliderBtn.isEnabled = true

        selfieVideoSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_pink, null)
        selfieVideoSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.lipstick, null)
    }

    private fun disableSubmitButton() {
        selfieVideoSubmitSliderBtn.isEnabled = false

        selfieVideoSubmitSliderBtn.outerColor =
            ResourcesCompat.getColor(resources, R.color.light_grey, null)
        selfieVideoSubmitSliderBtn.innerColor =
            ResourcesCompat.getColor(resources, R.color.warm_grey, null)
    }

    private inner class CameraListener : com.otaliastudios.cameraview.CameraListener() {

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            //User Image
            Log.d("Test", "Picture Taken")
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 2321
    }

}