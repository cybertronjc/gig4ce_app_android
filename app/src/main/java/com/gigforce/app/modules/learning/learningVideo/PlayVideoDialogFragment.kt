package com.gigforce.app.modules.learning.learningVideo


import android.app.Dialog
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.courseContent.CourseContentListFragment
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.slides.types.VideoWithTextFragment
import com.gigforce.app.utils.Lce
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_learning_slide_video_with_text.*
import kotlinx.android.synthetic.main.fragment_play_video.*
import kotlinx.android.synthetic.main.fragment_play_video_main.*
import kotlinx.android.synthetic.main.fragment_play_video_main.playerView


class PlayVideoDialogFragment : BaseFragment() {

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var currentOrientation =  ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private lateinit var mLessonId : String

    private var player: SimpleExoPlayer? = null
    private val viewModel : CourseVideoViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.let {
            playWhenReady = it.getBoolean("key_play_when_ready")
            currentWindow = it.getInt("key_current_video")
            playbackPosition = it.getLong("key_play_back_position")

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
        }

        arguments?.let {
            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
        }

        return inflateView(R.layout.fragment_play_video, inflater, container)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("key_play_when_ready", playWhenReady)
        outState.putInt("key_current_video", currentWindow)
        outState.putLong("key_play_back_position", playbackPosition)
        outState.putString(INTENT_EXTRA_LESSON_ID, mLessonId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()

        playerView
            .findViewById<View>(R.id.toggle_full_screen)
            .setOnClickListener {

                when (currentOrientation) {
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT-> {
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
    }

    private fun initViewModel() {
        viewModel.videoDetails
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showVideoAsLoading()
                    is Lce.Content -> showVideo(it.content)
                    is Lce.Error -> showErrorInLoadingVideo(it.error)
                }
            })

        viewModel.getVideoDetails(mLessonId)
    }

    private fun showErrorInLoadingVideo(error: String) {
        fragment_play_video_progress_bar.gone()
        fragment_play_video_main_layout.gone()

        fragment_play_video_error.visible()
        fragment_play_video_error.text = error
    }

    private fun showVideo(content: CourseContent) {
        fragment_play_video_progress_bar.gone()
        fragment_play_video_error.gone()
        fragment_play_video_main_layout.visible()

        val videoUri = Uri.parse(content.videoUrl)
        initializePlayer(videoUri)
    }

    private fun showVideoAsLoading() {
        fragment_play_video_main_layout.gone()
        fragment_play_video_error.gone()
        fragment_play_video_progress_bar.visible()
    }

    private fun adjustUiforOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                Log.d(VideoWithTextFragment.TAG, "PORTRAIT")

                val scale = resources.displayMetrics.density
                val pixels = (303 * scale + 0.5f).toInt()

                playerView.layoutParams.height = pixels
                playerView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                Log.d(VideoWithTextFragment.TAG, "LANDSCAPE")

                playerView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
                playerView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }

    override fun onBackPressed(): Boolean {

        return if(currentOrientation ==  ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){

            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            adjustUiforOrientation()
            return true
        }else false
    }

    private fun initializePlayer(uri : Uri) {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        playerView.player = player

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

    companion object {
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
    }

}