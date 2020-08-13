package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
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

        askCameraPermission.setOnClickListener {

            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
            intent.data = uri
            startActivityForResult(intent, REQUEST_CAMERA_PERMISSION)
        }

        recordVideoButton.setOnClickListener {

            if (capturingVideo) {
                //Stop capture
                capturingVideo = false
                cameraView.stopVideo()
            } else {
                capturingVideo = true
                recordingTimerLayout.visibility = View.VISIBLE
                recordVideoButton.setImageResource(R.drawable.ic_stop)
                startRecordingVideo()
                startTimer()
            }
        }
    }

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

    private fun startTimer() {
        timer.start()
    }

    private fun hasCameraPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {

        cameraMainLayout.gone()
        cameraPermissionLayout.visible()

        requestPermissions(
                arrayOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_CAMERA_PERMISSION
        )
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermissions())
            initCamera()
    }

    override fun onDestroy() {
        super.onDestroy()
        timer.cancel()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {

                var allPermsGranted = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        allPermsGranted = false
                        break
                    }
                }

                if (allPermsGranted)
                    initCamera()
                else
                    showPermissionLayout()
            }
        }
    }

    private fun initCamera() {
        cameraPermissionLayout.gone()
        cameraMainLayout.visible()

        CameraLogger.setLogLevel(CameraLogger.LEVEL_VERBOSE)
        cameraView.setLifecycleOwner(this)
        cameraView.addCameraListener(CameraListener())
    }

    private fun showPermissionLayout() {
        cameraMainLayout.gone()
        cameraPermissionLayout.visible()
    }

    private inner class CameraListener : com.otaliastudios.cameraview.CameraListener() {

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            recordVideoButton.setImageResource(R.drawable.ic_record)
            recordingTimerLayout.visibility = View.GONE

            if (capturingVideo)
                mCaptureVideoFragmentEventListener.videoCaptured(result.file)
            else {
                //Video Cpature Was cancelled In Mid
                try {
                    result.file.delete()
                } catch (e: Exception) {
                    //Dissolve
                }
            }

            capturingVideo = false
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CAMERA_MANUAL){
            if (hasCameraPermissions())
                initCamera()
        }

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
        private const val REQUEST_CAMERA_MANUAL = 2322
        private const val SELFIE_VIDEO_TIME = 10_000 //10 Secs

        fun getInstance(captureVideoFragmentEventListener: CaptureVideoFragmentEventListener): CaptureSelfieVideoFragment {
            return CaptureSelfieVideoFragment()
                    .apply {
                        mCaptureVideoFragmentEventListener = captureVideoFragmentEventListener
                    }
        }
    }

}