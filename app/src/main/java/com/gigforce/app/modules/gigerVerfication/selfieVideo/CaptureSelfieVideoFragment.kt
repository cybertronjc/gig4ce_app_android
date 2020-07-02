package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.DateHelper
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import kotlinx.android.synthetic.main.fragment_add_selfie_capture_video.*
import java.io.File

interface CaptureVideoFragmentEventListener {
    fun videoCaptured(file: File)
}

class CaptureSelfieVideoFragment : BaseFragment() {

    private lateinit var mCaptureVideoFragmentEventListener: CaptureVideoFragmentEventListener
    private var capturingVideo = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_selfie_capture_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (hasCameraPermissions())
            initCamera()
        else
            requestCameraPermission()

        recordVideoButton.setOnClickListener {

            if (!capturingVideo) {
                capturingVideo = true

                recordingTimerLayout.visibility = View.VISIBLE
                startRecordingVideo()
                startTimer()
            }
        }
    }

    private fun startTimer() {
        val timer =
            object : CountDownTimer(SELFIE_VIDEO_TIME.toLong() + 1000, 1000) {
                override fun onFinish() {
                    countDownTimerTV.text = "00:00"
                }

                override fun onTick(millisUntilFinished: Long) {

                    if (millisUntilFinished >= 10_000)
                        countDownTimerTV.text = "00:${millisUntilFinished / 1000}"
                    else
                        countDownTimerTV.text = "00:0${millisUntilFinished / 1000}"
                }
            }
        timer.start()
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

    private inner class CameraListener : com.otaliastudios.cameraview.CameraListener() {

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            capturingVideo = false
            mCaptureVideoFragmentEventListener.videoCaptured(result.file)
        }
    }

    private fun startRecordingVideo() {
        if (cameraView.mode == Mode.PICTURE) {
            Log.e("AddSelfieVideoFragment", "Camera Is In Picture Mode, Skipping Record Video")
            return
        }

        if (cameraView.isTakingPicture || cameraView.isTakingVideo)
            return

        val videoPath =
            File(requireContext().filesDir, "vid_${DateHelper.getFullDateTimeStamp()}.mp4")
        cameraView.takeVideo(videoPath, SELFIE_VIDEO_TIME)
    }

//    private fun stashRecordedVideoAndShowPreview() {
//        if (videoPath != null)
//            deleteExistingVideoIfExist()
//
//        hideVideoPreviewControls()
//        hideVideoPreviewControls()
//    }

    companion object {
        const val TAG = "CaptureSelfieVideoFragment"
        private const val REQUEST_CAMERA_PERMISSION = 2321
        private const val SELFIE_VIDEO_TIME = 10_000 //10 Secs

        fun getInstance(captureVideoFragmentEventListener: CaptureVideoFragmentEventListener): CaptureSelfieVideoFragment {
            return CaptureSelfieVideoFragment()
                .apply {
                    mCaptureVideoFragmentEventListener = captureVideoFragmentEventListener
                }
        }
    }

}