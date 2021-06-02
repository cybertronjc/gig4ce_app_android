package com.gigforce.verification.gigerVerfication.selfieVideo

import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.verification.R
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_add_selfie_play_selfie_video.*
import kotlinx.android.synthetic.main.fragment_add_selfie_play_selfie_video_controls.view.*
import java.io.File

interface PlaySelfieVideoFragmentEventListener {
    fun discardCurrentVideoAndStartRetakingVideo()
}

class PlaySelfieVideoFragment : Fragment() {

    private lateinit var mPlaySelfieVideoFragmentEventListener: PlaySelfieVideoFragmentEventListener

    private var player: SimpleExoPlayer? = null
    private lateinit var uri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_add_selfie_play_selfie_video, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (savedInstanceState != null) {

            if (savedInstanceState.getString(INTENT_EXTRA_URI) != null)
                uri = Uri.parse(savedInstanceState.getString(INTENT_EXTRA_URI))
        } else {

            if (arguments?.getString(INTENT_EXTRA_URI) != null)
                uri = Uri.parse(arguments?.getString(INTENT_EXTRA_URI))
        }


        if (Build.VERSION.SDK_INT > 23)
            initVideoPlayer()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_URI, uri.toString())
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
        player = SimpleExoPlayer.Builder(requireContext()).build()
        selfieVideoPlayerView.player = player
        selfieVideoPlayerView.retake_video.setOnClickListener {
            mPlaySelfieVideoFragmentEventListener.discardCurrentVideoAndStartRetakingVideo()
        }

        selfieVideoPlayerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING

        playVideo(uri)
    }

    fun playVideo(file: File) {
        val uri = Uri.fromFile(file)
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = false
        player?.seekTo(0, 0)
        player?.prepare(mediaSource, false, false)
    }

    fun playVideo(uri: Uri) {
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = false
        player?.seekTo(0, 0)
        player?.prepare(mediaSource, false, false)
    }


    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    private fun releasePlayer() {
        player?.release()
        player = null
    }

    fun showVideoUploadingProgress() {
        selfieVideoPlayerView.visibility = View.GONE
        videoUploadingLayout.visibility = View.VISIBLE
    }

    fun showPlayVideoLayout() {
        videoUploadingLayout.visibility = View.GONE
        selfieVideoPlayerView.visibility = View.VISIBLE
    }

    companion object {
        const val TAG = "PlaySelfieVideoFragment"
        private const val INTENT_EXTRA_URI = "uri"


        fun getInstance(
            playSelfieVideoFragmentEventListener: PlaySelfieVideoFragmentEventListener,
            uri: Uri
        ): PlaySelfieVideoFragment {
            return PlaySelfieVideoFragment()
                .apply {
                    val bundle = Bundle()
                    bundle.putString(INTENT_EXTRA_URI, uri.toString())
                    this.arguments = bundle

                    this.mPlaySelfieVideoFragmentEventListener =
                        playSelfieVideoFragmentEventListener
                }
        }
    }
}