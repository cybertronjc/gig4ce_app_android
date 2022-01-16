package com.gigforce.modules.feature_chat.mediapicker

import android.app.Activity
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.net.toUri
import androidx.databinding.DataBindingUtil
import com.gigforce.common_image_picker.databinding.ActivityImageCropBinding
import com.gigforce.common_image_picker.image_cropper.ImageCropActivity
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.modules.feature_chat.R
import com.gigforce.modules.feature_chat.ViewFullScreenVideoDialogFragment
import com.gigforce.modules.feature_chat.databinding.ActivityVideoViewerBinding
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_play_video_full_screen.*
import javax.inject.Inject

@AndroidEntryPoint
class VideoViewerActivity : AppCompatActivity() {
    companion object {
        const val PREFIX: String = "IMG"
        const val EXTENSION: String = ".jpg"
        const val CROP_RESULT_CODE = 90

        const val INTENT_EXTRA_INCOMING_URI = "INCOMING_URI"
        const val INTENT_EXTRA_TEXT = "SEND_TEXT"

        /**w
         *
         */
        const val INTENT_EXTRA_DESTINATION_URI = "destination_uri"

    }

    private lateinit var viewBinding: ActivityVideoViewerBinding
    private var cropImageUri: Uri? = null
    private lateinit var incomingFile: String

    private var playWhenReady = true
    private var currentWindow = 0
    private var isVideoCompleted = false
    private var playbackPosition: Long = 0
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private var player: SimpleExoPlayer? = null

    @Inject
    lateinit var logger: GigforceLogger
    private var win: Window? = null
    private lateinit var uri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT != Build.VERSION_CODES.O) {
            requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }

        viewBinding = DataBindingUtil.setContentView(this, R.layout.activity_video_viewer)
        savedInstanceState?.let {
            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")

            uri = it.getString(INTENT_EXTRA_INCOMING_URI)?.toUri() ?: return@let
        }

        val extras = intent.extras
        if (extras != null) {
            incomingFile = extras.getString(INTENT_EXTRA_INCOMING_URI).toString()
            uri = extras.getString(INTENT_EXTRA_INCOMING_URI)?.toUri()!!
        }
        setVideoToViewer(incomingFile)
        initListeners()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("key_play_when_ready", playWhenReady)
        outState.putInt("key_current_video", currentWindow)
        outState.putLong("key_play_back_position", playbackPosition)
        outState.putString(INTENT_EXTRA_INCOMING_URI, uri.toString())
    }

    private fun initListeners() = viewBinding.apply{

        playButton.setOnClickListener {

            if (player?.isPlaying == true){
                Log.d("VIDEO", "playing")
                playWhenReady = false
                player?.playWhenReady = false
                player?.playbackState
                playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_arrow_24))
            } else if (playbackPosition == 0L){
                Log.d("VIDEO", " completed")
                playWhenReady = true
                player?.playWhenReady = true
                player?.playbackState
                playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24))
                initializePlayer(uri, 0)
            }

            else {
                Log.d("VIDEO", " not playing")
                playWhenReady = true
                player?.playWhenReady = true
                player?.playbackState
                playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24))
            }
        }

        backImageButton.setOnClickListener {
            onBackPressed()
        }

        imageViewStop.setOnClickListener {
            val intent = Intent()
            intent.putExtra(VideoViewerActivity.INTENT_EXTRA_INCOMING_URI, uri.toString())
            intent.putExtra(VideoViewerActivity.INTENT_EXTRA_TEXT, editTextMessage.text.toString())
            setResult(Activity.RESULT_OK, intent)
            finish()
        }

    }

    private fun setVideoToViewer(incomingFile: String) {
        uri.let { initializePlayer(it, playbackPosition) }
    }

    private fun initializePlayer(uri: Uri, lastTimePlayBackPosition: Long) {
        player = SimpleExoPlayer.Builder(this).build()
        viewBinding.playerViewStepVideo.player = player

        val mediaSource = buildMediaSource(uri)

        if (playbackPosition != 0L) {
            player?.seekTo(currentWindow, playbackPosition)
            player?.playWhenReady = true
            viewBinding.playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_pause_24))
        } else {
            player?.seekTo(currentWindow, lastTimePlayBackPosition)
            player?.playWhenReady = playWhenReady
        }

        player?.prepare(mediaSource, false, false)
        addListener()
    }

    private fun addListener() {
        player!!.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    // media actually playing
                    Log.d("VIDEO", "onPlayerStateChanged: media is actually completed")
                    viewBinding.playButton.setImageDrawable(resources.getDrawable(R.drawable.ic_baseline_play_arrow_24))
                }
            }
        })
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
        val dataSourceFactory = DefaultDataSourceFactory(this, "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }

    override fun onBackPressed() {
        //super.onBackPressed()
        AlertDialog.Builder(this)
            .setTitle("Alert")
            .setMessage("Are you sure you want to close?")
            .setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

}