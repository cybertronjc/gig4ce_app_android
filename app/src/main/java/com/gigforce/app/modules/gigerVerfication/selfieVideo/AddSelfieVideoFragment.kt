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
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.Lse
import com.otaliastudios.cameraview.CameraLogger
import com.otaliastudios.cameraview.VideoResult
import com.otaliastudios.cameraview.controls.Mode
import kotlinx.android.synthetic.main.fragment_add_selfie_video.*
import java.io.File

class AddSelfieVideoFragment : BaseFragment() {

    private val viewModel: SelfiVideoViewModel by viewModels()

    private var videoPath: File? = null
    private lateinit var playVideoFragment: PlayVideoFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_selfie_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.uploadSelfieState
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> Log.d("Himanshu", "loading")
                    Lse.Success -> Log.d("Himanshu", "success")
                    is Lse.Error -> Log.e("Himanshu", it.error)
                }
            })
    }


    private fun initViews() {

        playVideoFragment =
            childFragmentManager.findFragmentById(R.id.selfieVideoPlayerFragment) as PlayVideoFragment
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

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

        recordVideoButton.setOnClickListener {
            startRecordingVideo()
            startTimer()
        }
    }

    private fun startTimer() {
        val timer = object : CountDownTimer(SELFIE_VIDEO_TIME.toLong(), 1000) {
            override fun onFinish() {

            }

            override fun onTick(millisUntilFinished: Long) {
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

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            showToast("Video Recorded")
            videoPath = result.file

            // hideCaptureVideoControls()
            //showVideoPreviewControls()

            //  playVideoFragment.playVideo(result.file)
            viewModel.uploadSelfieVideo(result.file)
        }
    }

    private fun startRecordingVideo() {
        if (cameraView.mode == Mode.PICTURE) {
            Log.e("AddSelfieVideoFragment", "Camera Is In Picture Mode, Skipping Record Video")
            return
        }

        if (cameraView.isTakingPicture || cameraView.isTakingVideo)
            return

        videoPath = File(requireContext().filesDir, "vid_${DateHelper.getFullDateTimeStamp()}.mp4")
        cameraView.takeVideo(videoPath!!, SELFIE_VIDEO_TIME)
    }

    private fun stashRecordedVideoAndShowPreview() {
        if (videoPath != null)
            deleteExistingVideoIfExist()

        hideVideoPreviewControls()
        hideVideoPreviewControls()
    }

    private fun showCaptureVideoControls() {
        cameraViewContainer.visibility = View.VISIBLE
    }

    private fun hideCaptureVideoControls() {
        cameraViewContainer.visibility = View.GONE
    }

    private fun hideVideoPreviewControls() {
        videoPlayerContainer.visibility = View.INVISIBLE
    }

    private fun showVideoPreviewControls() {
        videoPlayerContainer.visibility = View.VISIBLE
    }

    private fun deleteExistingVideoIfExist() {
        runCatching {
            if (videoPath!!.exists())
                videoPath?.delete()

        }.onFailure {
            Log.e("AddSelfieVideoPath", "Unable to Delete Video", it)
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 2321
        private const val SELFIE_VIDEO_TIME = 5_000 //10 Secs
    }
}