package com.gigforce.app.modules.learning.slides.types

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_learning_slide_video_with_text.*
import kotlinx.android.synthetic.main.fragment_learning_slide_video_with_text.playerView
import kotlinx.android.synthetic.main.fragment_play_video.*

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
            description: String
        ): VideoWithTextFragment {
            return VideoWithTextFragment().apply {
                arguments = bundleOf(
                    KEY_LESSON_ID to lessonId,
                    KEY_SLIDE_ID to slideId,
                    KEY_VIDEO_URI to videoUri.toString(),
                    KEY_TITLE to title,
                    KEY_DESCRIPTION to description
                )
            }
        }
    }

    private lateinit var mLessonId: String
    private lateinit var mSlideId: String
    private lateinit var mVideoUri: Uri
    private lateinit var mTitle: String
    private lateinit var mDescription: String

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var mPlayer: SimpleExoPlayer? = null

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

        setVideoOnView()

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                video_slide_title_tv.text = mTitle
                video_slide_desc_tv.text = mDescription
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                slideInfoLayout.gone()
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

                when (resources.configuration.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> activity?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    Configuration.ORIENTATION_LANDSCAPE -> activity?.requestedOrientation =
                        ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }
            }

        if (Build.VERSION.SDK_INT > 23)
            initVideoPlayer()
    }


    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23)
            initVideoPlayer()
    }

    override fun onPause() {
        super.onPause()

        if (Build.VERSION.SDK_INT <= 23)
            releasePlayer()
    }

    override fun onStop() {
        super.onStop()

        if (Build.VERSION.SDK_INT > 23)
            releasePlayer()
    }

    private fun initVideoPlayer() {
        mPlayer = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = mPlayer

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


}