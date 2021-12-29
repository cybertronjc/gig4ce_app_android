package com.gigforce.modules.feature_chat


import android.app.Dialog
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import androidx.core.net.toUri
import androidx.fragment.app.DialogFragment
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.metaDataHelper.ImageMetaDataHelpers
import com.gigforce.common_ui.storage.MediaStoreApiHelpers
import com.gigforce.core.IEventTracker
import com.gigforce.core.TrackingEventArgs
import com.gigforce.modules.feature_chat.analytics.CommunityEvents
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_play_video_full_screen.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ViewFullScreenVideoDialogFragment : DialogFragment(), PopupMenu.OnMenuItemClickListener {

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private lateinit var uri: Uri
    private var player: SimpleExoPlayer? = null

    @Inject
    lateinit var eventTracker: IEventTracker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.let {
            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")

            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }

        arguments?.let {
            uri = it.getString(INTENT_EXTRA_URI)?.toUri() ?: return@let
        }

        return inflater.inflate(R.layout.fragment_play_video_full_screen, container, false)
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
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val optionsImageView : View = view.findViewById(R.id.options_iv)
        val backImageView : View = view.findViewById(R.id.back_iv)

        backImageView.setOnClickListener {
            dismiss()
        }

        optionsImageView.setOnClickListener {

            val popUpMenu = PopupMenu(context, it)
            popUpMenu.inflate(R.menu.menu_image_viewer)

            popUpMenu.setOnMenuItemClickListener(this)
            popUpMenu.show()
        }


        playerView
            .findViewById<View>(R.id.toggle_full_screen)
            .setOnClickListener {
                changeOrientation()
            }

        initializePlayer(uri, playbackPosition)
    }

    private fun changeOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                currentOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        }

        adjustUiforOrientation()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun onBackPressed() {
                backPressed()
            }
        }
    }


    private fun adjustUiforOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {

                val scale = resources.displayMetrics.density
                val pixels = (303 * scale + 0.5f).toInt()

                playerView?.layoutParams?.height = pixels
                playerView?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {

                playerView?.layoutParams?.height = LinearLayout.LayoutParams.MATCH_PARENT
                playerView?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }

    fun backPressed() {

        if (currentOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {

            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            adjustUiforOrientation()
        } else {
            player?.stop()
            dismiss()
        }
    }


    private fun initializePlayer(uri: Uri, lastTimePlayBackPosition: Long) {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player

        val mediaSource = buildMediaSource(uri)

        if (playbackPosition != 0L) {
            player?.seekTo(currentWindow, playbackPosition)
            player?.playWhenReady = true
        } else {
            player?.seekTo(currentWindow, lastTimePlayBackPosition)
            player?.playWhenReady = playWhenReady
        }

        player?.prepare(mediaSource, false, false)
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

    override fun onMenuItemClick(item: MenuItem?): Boolean {

        GlobalScope.launch {

            try {
                MediaStoreApiHelpers.saveVideoToGallery(
                    requireContext(),
                    uri
                )
                launch(Dispatchers.Main) {
                    showToast("Video saved in gallery")
                    var map = mapOf("media_type" to "Video")
                    eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_MEDIA_SAVED_TO_GALLERY, map))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                launch(Dispatchers.Main) {
                    showToast("Unable to save image in gallery")
                    var map = mapOf("media_type" to "Video")
                    eventTracker.pushEvent(TrackingEventArgs(CommunityEvents.EVENT_CHAT_MEDIA_FAILED_TO_SAVE, map))
                }
            }
        }

        return true
    }


    companion object {
        const val INTENT_EXTRA_URI = "uri"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
        const val TAG = "ViewFullScreenVideoDialogFragment"

//        fun launch(
//            childFragmentManager: FragmentManager,
//            uri: Uri
//        ) {
//            val frag = ViewFullScreenVideoDialogFragment()
//            val bundle = bundleOf(
//                INTENT_EXTRA_URI to uri.toString()
//            )
//
//            frag.arguments = bundle
//            frag.show(childFragmentManager, TAG)
//        }
    }


}