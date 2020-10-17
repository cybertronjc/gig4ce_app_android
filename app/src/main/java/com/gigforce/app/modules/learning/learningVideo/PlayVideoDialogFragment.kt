package com.gigforce.app.modules.learning.learningVideo


import android.app.Dialog
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.assessment.AssessmentFragment
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.slides.SlidesFragment
import com.gigforce.app.modules.learning.slides.types.VideoWithTextFragment
import com.gigforce.app.utils.Lce
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.fragment_play_video.*
import kotlinx.android.synthetic.main.fragment_play_video_main.*


class PlayVideoDialogFragment : DialogFragment() {

    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private lateinit var mLessonId: String
    private lateinit var mModuleId: String

    private var player: SimpleExoPlayer? = null
    private val viewModel: CourseVideoViewModel by viewModels()
    private var videoStateSaved: Boolean = false

    private val navigationController : NavController by lazy {
        requireActivity().findNavController(R.id.nav_fragment)
    }

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
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        arguments?.let {

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        return inflater.inflate(R.layout.fragment_play_video, container, false)
//        return inflateView(R.layout.fragment_play_video, inflater, container)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean("key_play_when_ready", playWhenReady)
        outState.putInt("key_current_video", currentWindow)
        outState.putLong("key_play_back_position", playbackPosition)
        outState.putString(INTENT_EXTRA_LESSON_ID, mLessonId)
        outState.putString(INTENT_EXTRA_MODULE_ID, mModuleId)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViewModel()

        close_click_layout.setOnClickListener {
            backPressed()
        }

        playerView
            .findViewById<View>(R.id.toggle_full_screen)
            .setOnClickListener {
                changeOrientation()
            }


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
        return object : Dialog(requireContext(),theme){
            override fun onBackPressed() {
               backPressed()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val winAttrib = dialog?.window?.attributes
        winAttrib?.dimAmount = 0.0f
        dialog?.window?.attributes = winAttrib
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

        viewModel.videoSaveState
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showVideoAsLoading()
                    is Lce.Content -> {
                        when (it.content) {
                            VideoSaveState.VideoStateSaved -> {
                                videoStateSaved = true
                                clearBackStackToContentList()
                                dismiss()
                            }
                            VideoSaveState.VideoMarkedComplete -> {
                                //Open next
                                videoStateSaved = true
                            }
                        }
                    }
                    is Lce.Error -> showErrorInLoadingVideo(it.error)
                }
            })

        viewModel.openNextDestination.observe(viewLifecycleOwner, Observer { cc ->


            val view = layoutInflater.inflate(R.layout.layout_video_completed, null)

            val dialog = AlertDialog.Builder(requireContext())
                .setView(view)
                .show()

            val submitTV = view.findViewById<TextView>(R.id.tv_action_assess_dialog)
            submitTV.text = when (cc?.type) {
                CourseContent.TYPE_ASSESSMENT -> "Next Assessment"
                CourseContent.TYPE_VIDEO -> "Next Lesson"
                CourseContent.TYPE_SLIDE -> "Next Slide"
                else -> "Okay"
            }

            submitTV.setOnClickListener {
                    dialog.dismiss()
                    dismiss()


                    when (cc?.type) {
                        CourseContent.TYPE_VIDEO -> {
                            PlayVideoDialogFragment.launch(
                                childFragmentManager = childFragmentManager,
                                moduleId = cc.moduleId,
                                lessonId = cc.id
                            )
                        }
                        CourseContent.TYPE_ASSESSMENT -> {
                            navigationController.navigate(
                                R.id.assessment_fragment, bundleOf(
                                    AssessmentFragment.INTENT_LESSON_ID to cc.id,
                                    AssessmentFragment.INTENT_MODULE_ID to cc.moduleId
                                )
                            )
                        }
                        CourseContent.TYPE_SLIDE -> {
                            navigationController.navigate(
                                R.id.slidesFragment,
                                bundleOf(
                                    SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to cc.title,
                                    SlidesFragment.INTENT_EXTRA_MODULE_ID to cc.moduleId,
                                    SlidesFragment.INTENT_EXTRA_LESSON_ID to cc.id
                                )
                            )
                        }
                        else ->{
                            clearBackStackToContentList()
                            dismiss()
                        }
                    }

                }

        })

        viewModel.getVideoDetails(mModuleId, mLessonId)
    }

    private fun clearBackStackToContentList() {
        try {
            navigationController.getBackStackEntry(R.id.assessmentListFragment)
            navigationController.popBackStack(R.id.assessmentListFragment, false)
        } catch (e: Exception) {

            try {
                navigationController.getBackStackEntry(R.id.courseContentListFragment)
                navigationController.popBackStack(R.id.courseContentListFragment, false)
            } catch (e: Exception) {

                try {
                    navigationController.getBackStackEntry(R.id.learningCourseDetails)
                    navigationController.popBackStack(R.id.learningCourseDetails, false)
                } catch (e: Exception) {

                    try {
                        navigationController.getBackStackEntry(R.id.mainLearningFragment)
                        navigationController.popBackStack(R.id.mainLearningFragment, false)
                    } catch (e: Exception) {

                    }
                }
            }
        }
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
        initializePlayer(videoUri, content.completionProgress)
        changeOrientation()

        if(!content.canUserFastForward){
            playerView.setControlDispatcher(PositionLimitingControlDispatcher())
        }
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

                playerView?.layoutParams?.height = pixels
                playerView?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT

                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                Log.d(VideoWithTextFragment.TAG, "LANDSCAPE")

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
            if (player == null || videoStateSaved){
                clearBackStackToContentList()
                dismiss()
            }

            player?.stop()
            val currentPos = player?.currentPosition ?: 0L
            val totalLenght = player?.duration ?: 0L
            viewModel.savedVideoState(
                moduleId = mModuleId,
                lessonId = mLessonId,
                playBackPosition = currentPos,
                fullVideoLength = totalLenght
            )
        }
    }


    private fun initializePlayer(uri: Uri, lastTimePlayBackPosition: Long) {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        player?.addListener(PlayerEventListener())
        playerView.player = player

        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT

        val mediaSource = buildMediaSource(uri)


        if (playbackPosition != 0L) {
            player?.seekTo(currentWindow, playbackPosition)
            player?.playWhenReady = true
        }
        else {
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

    companion object {
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
        const val TAG = "PlayVideoDialogFragment"

        fun launch(
            childFragmentManager: FragmentManager,
            moduleId: String,
            lessonId: String
        ) {
            val frag = PlayVideoDialogFragment()
            val bundle = bundleOf(
                INTENT_EXTRA_MODULE_ID to moduleId,
                INTENT_EXTRA_LESSON_ID to lessonId
            )

            frag.arguments = bundle
            frag.show(childFragmentManager, TAG)
        }
    }

    inner class PlayerEventListener : Player.EventListener {


        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)

            if (playbackState == Player.STATE_IDLE || !playWhenReady) {
                playerView.keepScreenOn = false
            }else if (playbackState == Player.STATE_ENDED) {
                playerView.keepScreenOn = false
                viewModel.markVideoAsComplete(mModuleId, mLessonId)
            }else { // STATE_IDLE, STATE_ENDED
                // This prevents the screen from getting dim/lock
                playerView.keepScreenOn = true
            }
        }
    }
}