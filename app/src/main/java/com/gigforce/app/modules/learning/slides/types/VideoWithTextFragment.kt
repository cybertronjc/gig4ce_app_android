package com.gigforce.app.modules.learning.slides.types

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_learning_slide_video_with_text.*

class VideoWithTextFragment : BaseFragment() {

    companion object {
        const val TAG = "VideoWithTextFragment"

        private const val KEY_LESSON_ID = "lesson_id"
        private const val KEY_SLIDE_ID = "slide_id"
        private const val KEY_VIDEO_URI = "video_uri"
        private const val KEY_TITLE = "title"
        private const val KEY_DESCRIPTION = "description"

        fun getInstance(
            lessonId: String,
            slideId: String,
            videoUri: Uri,
            title: String,
            description: String,
            baseFragOrientationListener: VideoFragmentOrientationListener
        ): VideoWithTextFragment {
            return VideoWithTextFragment().apply {
                arguments = bundleOf(
                    KEY_LESSON_ID to lessonId,
                    KEY_SLIDE_ID to slideId,
                    KEY_VIDEO_URI to videoUri.toString(),
                    KEY_TITLE to title,
                    KEY_DESCRIPTION to description
                )

                this.baseFragOrientationListener = baseFragOrientationListener
            }
        }
    }

    private lateinit var mLessonId: String
    private lateinit var mSlideId: String
    private lateinit var mVideoUri: Uri
    private lateinit var mTitle: String
    private lateinit var mDescription: String
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private var playWhenReady = false
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var mPlayer: SimpleExoPlayer? = null
    private var baseFragOrientationListener: VideoFragmentOrientationListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflateView(R.layout.fragment_learning_slide_video_with_text, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mSlideId = it.getString(KEY_SLIDE_ID) ?: return@let
            mVideoUri = it.getString(KEY_VIDEO_URI)?.toUri() ?: return@let
            mTitle = it.getString(KEY_TITLE) ?: return@let
            mDescription = it.getString(KEY_DESCRIPTION) ?: return@let
        }

        savedInstanceState?.let {

            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")

            mLessonId = it.getString(KEY_LESSON_ID) ?: return@let
            mSlideId = it.getString(KEY_SLIDE_ID) ?: return@let
            mVideoUri = it.getString(KEY_VIDEO_URI)?.toUri() ?: return@let
            mTitle = it.getString(KEY_TITLE) ?: return@let
            mDescription = it.getString(KEY_DESCRIPTION) ?: return@let
        }


    }

    private fun adjustUiforOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                Log.d(TAG, "PORTRAIT")
                slideInfoLayout.visible()
                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                Log.d(TAG, "LANDSCAPE")
                slideInfoLayout.gone()
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {

            putBoolean("key_play_when_ready", playWhenReady)
            putInt("key_current_video", currentWindow)
            putLong("key_play_back_position", playbackPosition)

            putString(KEY_LESSON_ID, mLessonId)
            putString(KEY_SLIDE_ID, mSlideId)
            putString(KEY_VIDEO_URI, mVideoUri.toString())
            putString(KEY_TITLE, mTitle)
            putString(KEY_DESCRIPTION, mDescription)
        }
    }

    private fun setVideoOnView() {

        playerView
            .findViewById<View>(R.id.toggle_full_screen)
            .setOnClickListener {

                when (currentOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        baseFragOrientationListener?.onOrientationChange(true)
                    }
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        baseFragOrientationListener?.onOrientationChange(false)
                    }
                }

                adjustUiforOrientation()
            }

        initVideoPlayer()
    }


    override fun onResume() {
        super.onResume()
        setVideoOnView()
    }

    override fun onPause() {
        super.onPause()
        mPlayer?.stop()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun initVideoPlayer() {
        mPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = mPlayer

        video_slide_title_tv.text = mTitle

        if (mDescription.length >= 160)
            video_slide_desc_tv.text = getDescriptionText(mDescription.substring(0, 160))
        else
            video_slide_desc_tv.text = mDescription

        video_slide_desc_tv.setOnClickListener {
            showText(mDescription)
        }

        video_slide_title_tv.text = "Video Title"
        video_slide_desc_tv.text = "Video description"


        //     slideVideoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
//        mPlayer?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

        playVideo(mVideoUri)
    }


    private fun playVideo(uri: Uri) {
        val mediaSource = buildMediaSource(uri)

        mPlayer?.playWhenReady = playWhenReady
        mPlayer?.seekTo(currentWindow, playbackPosition)
        mPlayer?.prepare(mediaSource, false, false)
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun releasePlayer() {
        if (mPlayer != null) {
            playbackPosition = mPlayer!!.currentPosition
            currentWindow = mPlayer!!.currentWindowIndex
            playWhenReady = mPlayer!!.playWhenReady
            mPlayer!!.release()
            mPlayer = null
        }
    }


    private fun getDescriptionText(text: String): SpannableString {
        if (text.isBlank())
            return SpannableString("")

        val string = SpannableString(text + SingleImageFragment.READ_MORE)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.white, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), text.length + 3, string.length - 1, 0)
        string.setSpan(UnderlineSpan(), text.length + 2, string.length, 0)

        return string
    }

    private fun showText(text: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_show_text, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = text

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.okay_text) { _, _ -> }
            .show()
    }

    fun backButtonPressed() : Boolean{

        return if(currentOrientation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){

            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            adjustUiforOrientation()
            return true
        }else false
    }

}

interface VideoFragmentOrientationListener {

    fun onOrientationChange(landscape: Boolean)
}