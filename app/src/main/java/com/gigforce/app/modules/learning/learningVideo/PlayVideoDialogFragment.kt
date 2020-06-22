package com.gigforce.app.modules.learning.learningVideo


import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_play_video.*


class PlayVideoDialogFragment : DialogFragment() {

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private var player: SimpleExoPlayer? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {

        savedInstanceState?.let {
            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")
        }
        return inflater.inflate(R.layout.fragment_play_video, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("key_play_when_ready", playWhenReady)
        outState.putInt("key_current_video", currentWindow)
        outState.putLong("key_play_back_position", playbackPosition)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player

        val uri =
                Uri.parse("https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/learning_videos%2FM1L2_2.mp4?alt=media&token=7cbacffa-6d28-431d-bf0f-04ce388af935")
        val mediaSource = buildMediaSource(uri)

        player?.playWhenReady = playWhenReady
        player?.seekTo(currentWindow, playbackPosition)
        player?.prepare(mediaSource, false, false)
    }

    private fun releasePlayer() {
        if (player != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
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

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (Build.VERSION.SDK_INT > 23)
            initializePlayer()

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
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT <= 23)
            initializePlayer()
    }


    override fun onStart() {
        super.onStart()
        val window = dialog?.window
        val windowParams = window?.attributes
        windowParams?.dimAmount = 0.0f
        window?.attributes = windowParams
    }

}