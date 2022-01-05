package com.gigforce.modules.feature_chat.screens

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.ViewFullScreenVideoDialogFragment
import com.gigforce.modules.feature_chat.databinding.FragmentAudioPlayerBottomSheetBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_play_video_full_screen.*

@AndroidEntryPoint
class AudioPlayerBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentAudioPlayerBottomSheetBinding>(
    fragmentName = "AudioPlayerBottomSheetFragment",
    layoutId = R.layout.fragment_audio_player_bottom_sheet
) {

    companion object {

        const val TAG = "AudioPlayerBottomSheetFragment"
        const val INTENT_EXTRA_URI = "uri"

    }

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0

    private lateinit var uri: Uri
    private var player: SimpleExoPlayer? = null

    override fun viewCreated(
        viewBinding: FragmentAudioPlayerBottomSheetBinding,
        savedInstanceState: Bundle?
    ) {
        savedInstanceState?.let {
            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")

            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }

        arguments?.let {
            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }
        if(uri != null) initializePlayer(uri, playbackPosition)
        initListeners()
    }

    private fun initListeners() {
        viewBinding.playPauseAudio.setOnClickListener {
            if (player?.isPlaying == true) {
                viewBinding.playPauseAudio.setImageDrawable(context?.getDrawable(R.drawable.ic_pink_play_icon))
                player?.playWhenReady = false
            } else{
                viewBinding.playPauseAudio.setImageDrawable(context?.getDrawable(R.drawable.ic_pink_pause_icon))
                player?.playWhenReady = true
            }
        }

        viewBinding.closeAudio.setOnClickListener {
            releasePlayer()
            dismiss()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("key_play_when_ready", playWhenReady)
        outState.putInt("key_current_video", currentWindow)
        outState.putLong("key_play_back_position", playbackPosition)
        outState.putString(INTENT_EXTRA_URI, uri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }

    private fun initializePlayer(uri: Uri, lastTimePlayBackPosition: Long) {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        //playerView.player = player

        val mediaSource = buildMediaSource(uri)

        if (playbackPosition != 0L) {
            val seekTo = playbackPosition.toFloat()
            player?.seekTo(currentWindow, playbackPosition)
            seekTo.also { viewBinding.slider.value = it }
            player?.playWhenReady = true
        } else {
            player?.seekTo(currentWindow, lastTimePlayBackPosition)
            player?.playWhenReady = playWhenReady
            lastTimePlayBackPosition.toFloat().also { viewBinding.slider.value = it }
        }

        player?.prepare(mediaSource, false, false)
        viewBinding.playPauseAudio.setImageDrawable(context?.getDrawable(R.drawable.ic_pink_pause_icon))
    }

    override fun onResume() {
        super.onResume()
        player?.playWhenReady = true
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
        player?.playWhenReady = false
    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }

    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

}