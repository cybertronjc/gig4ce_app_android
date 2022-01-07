package com.gigforce.modules.feature_chat.screens

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.chat.ChatFileManager
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.ViewFullScreenVideoDialogFragment
import com.gigforce.modules.feature_chat.databinding.FragmentAudioPlayerBottomSheetBinding
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.material.bottomsheet.BottomSheetBehavior
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.android.synthetic.main.fragment_play_video_full_screen.*
import java.io.File
import java.io.IOException

@AndroidEntryPoint
class AudioPlayerBottomSheetFragment : BaseBottomSheetDialogFragment<FragmentAudioPlayerBottomSheetBinding>(
    fragmentName = "AudioPlayerBottomSheetFragment",
    layoutId = R.layout.fragment_audio_player_bottom_sheet
) {

    companion object {

        const val TAG = "AudioPlayerBottomSheetFragment"
        const val INTENT_EXTRA_URI = "uri"

    }


    private lateinit var uri: Uri

    private val chatFileManager: ChatFileManager by lazy {
        ChatFileManager(requireContext())
    }

    //for media player
    var mediaPlayer: MediaPlayer? = null
    var isPlaying: Boolean = false
    var isCompleted: Boolean = false
    var fileToPlay: File? = null
    var seekBarHandler: Handler? = null
    var updateSeekBar: Runnable? = null

    override fun viewCreated(
        viewBinding: FragmentAudioPlayerBottomSheetBinding,
        savedInstanceState: Bundle?
    ) {
        savedInstanceState?.let {
            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }

        arguments?.let {
            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }
        if(uri != null){
            Log.d(TAG, "uri received: $uri")
            fileToPlay = File(chatFileManager.audioFilesDirectory,uri.lastPathSegment)
            playAudio(fileToPlay)
        }
        initListeners()
    }

    private fun initListeners() {
        viewBinding.playPauseAudio.setOnClickListener {
            if (isPlaying) {
                pauseAudio()
            } else {
                if (fileToPlay != null && isCompleted) {
                    playAudio(fileToPlay)
                } else {
                    resumeAudio()
                }

            }
        }

        viewBinding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                if (fileToPlay != null) {
                    pauseAudio()
                }

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                if (fileToPlay != null) {
                    val progress = seekBar!!.progress
                    mediaPlayer!!.seekTo(progress)
                    resumeAudio()
                }

            }

        })

        viewBinding.closeAudio.setOnClickListener {
            stopAudio()
            dismiss()
        }
    }

    private fun stopAudio() {
        viewBinding.playPauseAudio.setImageDrawable(
            activity?.resources?.getDrawable(
                R.drawable.ic_baseline_play_arrow_24,
                null
            )
        )
        isPlaying = false
        mediaPlayer!!.stop()
        seekBarHandler!!.removeCallbacks(updateSeekBar!!)
    }

    private fun playAudio(file: File?) {
        mediaPlayer = MediaPlayer()

        try {
            mediaPlayer!!.setDataSource(fileToPlay!!.absolutePath)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            Log.d(TAG, "file: ${fileToPlay!!.path}")
        } catch (e: IOException) {
            Log.d(TAG, "exc: ${e.message}")
            showToast("Error playing this audio")
            dismiss()
            e.printStackTrace()
        }
        viewBinding.playPauseAudio.setImageDrawable(
            activity?.resources?.getDrawable(
                R.drawable.ic_pink_pause_icon,
                null
            )
        )
        Log.d(TAG, "playing")
        isPlaying = true

        mediaPlayer!!.setOnCompletionListener {
            isPlaying = false
            isCompleted = true
            stopAudio()
        }
        viewBinding.seekBar.max = mediaPlayer!!.duration
        seekBarHandler = Handler()
        updateRunnable()

        seekBarHandler!!.postDelayed(updateSeekBar!!, 0)

    }

    private fun updateRunnable() {
        updateSeekBar = object : Runnable {
            override fun run() {
                viewBinding.seekBar.progress = mediaPlayer!!.currentPosition
                seekBarHandler!!.postDelayed(this, 500)
            }
        }
    }

    private fun pauseAudio() {
        viewBinding.playPauseAudio.setImageDrawable(
            activity?.resources?.getDrawable(
                R.drawable.ic_baseline_play_arrow_24,
                null
            )
        )
        mediaPlayer!!.pause()
        isPlaying = false
        seekBarHandler!!.removeCallbacks(updateSeekBar!!)
    }

    private fun resumeAudio() {
        viewBinding.playPauseAudio.setImageDrawable(
            activity?.resources?.getDrawable(
                R.drawable.ic_pink_pause_icon,
                null
            )
        )
        mediaPlayer!!.start()
        isPlaying = true
        updateRunnable()
        seekBarHandler!!.postDelayed(updateSeekBar!!, 0)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_URI, uri.toString())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle)
    }


    override fun onPause() {
        super.onPause()
        stopAudio()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAudio()
    }

}