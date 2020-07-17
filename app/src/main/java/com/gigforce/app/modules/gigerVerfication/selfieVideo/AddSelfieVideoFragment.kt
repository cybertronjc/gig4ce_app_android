package com.gigforce.app.modules.gigerVerfication.selfieVideo

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.gigerVerfication.panCard.AddPanCardInfoFragment
import com.gigforce.app.utils.DateHelper
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.storage.FirebaseStorage
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.fragment_add_selfie_video.*
import java.io.File

class AddSelfieVideoFragment : BaseFragment(), CaptureVideoFragmentEventListener,
    PlaySelfieVideoFragmentEventListener {

    private val viewModel: SelfiVideoViewModel by viewModels()

    private var mCapturedVideoPath: File? = null

    private lateinit var captureSelfieVideoFragment: CaptureSelfieVideoFragment
    private lateinit var playSelfieVideoFragment: PlaySelfieVideoFragment
    private val firebaseStorage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_add_selfie_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        // addCaptureVideoFragment()
        initViewModel()
    }

    private fun addCaptureVideoFragment() {
        captureSelfieVideoFragment = CaptureSelfieVideoFragment.getInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(
            R.id.selfieVideoAndPlayContainer,
            captureSelfieVideoFragment,
            CaptureSelfieVideoFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun addPlayVideoFragment(remoteUri: Uri) {
        playSelfieVideoFragment = PlaySelfieVideoFragment.getInstance(this, remoteUri)
        val transaction = childFragmentManager.beginTransaction()
        transaction.add(
            R.id.selfieVideoAndPlayContainer,
            playSelfieVideoFragment,
            PlaySelfieVideoFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun replaceCaptureFragmentWithPreviewFragment(file: File) {
        playSelfieVideoFragment = PlaySelfieVideoFragment.getInstance(this, file.toUri())
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(
            R.id.selfieVideoAndPlayContainer,
            playSelfieVideoFragment,
            PlaySelfieVideoFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun replacePlayVideoFragmentWithCaptureFragment() {
        captureSelfieVideoFragment = CaptureSelfieVideoFragment.getInstance(this)
        val transaction = childFragmentManager.beginTransaction()
        transaction.replace(
            R.id.selfieVideoAndPlayContainer,
            captureSelfieVideoFragment,
            CaptureSelfieVideoFragment.TAG
        )
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
        transaction.commit()
    }

    private fun initViewModel() {
        viewModel.uploadSelfieState
            .observe(viewLifecycleOwner, Observer {
                when (it) {
                    Lse.Loading -> {
                        playSelfieVideoFragment.showVideoUploadingProgress()
                    }
                    Lse.Success -> {
                        showToast("Video Uploaded")
                        navigate(R.id.addPanCardInfoFragment, Bundle()
                            .apply {
                                this.putBoolean(
                                    AddPanCardInfoFragment.CAME_FROM_SELFIE_SCREEN,
                                    true
                                )
                            })
                    }
                    is Lse.Error -> {
                        playSelfieVideoFragment.showPlayVideoLayout()
                        selfieVideoSubmitSliderBtn.resetSlider()
                        MaterialAlertDialogBuilder(requireContext())
                            .setMessage(it.error)
                            .show()
                    }
                }
            })

        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.selfieVideoUploaded) {
                    if (::captureSelfieVideoFragment.isInitialized) {
                        //Video Just Got Uploaded
                    } else {
                        //Cold Start
                        val videoRef = firebaseStorage
                            .reference
                            .child("verification_selfie_videos")
                            .child(it.selfieVideoDataModel!!.videoPath)

                        videoRef.downloadUrl.addOnSuccessListener {
                            addPlayVideoFragment(it)
                        }.addOnFailureListener {
                            it.printStackTrace()
                        }

                    }
                } else {
                    addCaptureVideoFragment()
                }
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }


    private fun initViews() {
        toolbar.setNavigationOnClickListener { activity?.onBackPressed() }

        selfieVideoCorrectCB.setOnCheckedChangeListener { buttonView, isChecked ->

            if (isChecked && mCapturedVideoPath != null)
                enableSubmitButton()
            else
                disableSubmitButton()
        }

        selfieVideoSubmitSliderBtn.onSlideCompleteListener =
            object : SlideToActView.OnSlideCompleteListener {

                override fun onSlideComplete(view: SlideToActView) {
                    val transcodedFile = File(
                        requireContext().filesDir,
                        "vid_${DateHelper.getFullDateTimeStamp()}.mp4"
                    )
                    viewModel.uploadSelfieVideo(mCapturedVideoPath!!, transcodedFile)
                }
            }
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

    private fun deleteExistingVideoIfExist() {
        runCatching {
            if (mCapturedVideoPath!!.exists())
                mCapturedVideoPath?.delete()

            mCapturedVideoPath = null
        }.onFailure {
            Log.e("AddSelfieVideoPath", "Unable to Delete Video", it)
        }
    }

    override fun videoCaptured(file: File) {
        showToast("Video Recorded")
        this.mCapturedVideoPath = file
        replaceCaptureFragmentWithPreviewFragment(file)

        if (selfieVideoCorrectCB.isChecked)
            enableSubmitButton()
    }

    override fun discardCurrentVideoAndStartRetakingVideo() {
        deleteExistingVideoIfExist()
        replacePlayVideoFragmentWithCaptureFragment()

        selfieVideoCorrectCB.isChecked = false
        disableSubmitButton()
    }


}