package com.gigforce.app.modules.client_activation

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.slides.types.VideoWithTextFragment
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import kotlinx.android.synthetic.main.layout_fragment_activation_gig.*

class GigActivationFragment : BaseFragment(),
    AdapterGigActivation.AdapterApplicationClientActivationCallbacks {
    private lateinit var viewModel: GigActivationViewModel
    private lateinit var mJobProfileId: String
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    var playerViewHeight = 0
    private var isPlayingVideo: Boolean? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_activation_gig, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        val layoutParams: ConstraintLayout.LayoutParams =
            cv_player_view.layoutParams as ConstraintLayout.LayoutParams
        playerViewHeight = (getScreenWidth(requireActivity()).height * 45) / 100
        layoutParams.height = playerViewHeight
        cv_player_view.layoutParams = layoutParams
        viewModel =
            ViewModelProvider(
                this,
                SavedStateViewModelFactory(requireActivity().application, this)
            ).get(GigActivationViewModel::class.java)
        checkForBackPress()
        setupRecycler()
        initObservers()
        initClicks()

    }

    private fun initClicks() {
        iv_back_application_gig_activation.setOnClickListener { popBackState() }
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

    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    private fun adjustUiforOrientation() {
        when (currentOrientation) {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT -> {
                Log.d(VideoWithTextFragment.TAG, "PORTRAIT")
                val layoutParams: ConstraintLayout.LayoutParams =
                    cv_player_view.layoutParams as ConstraintLayout.LayoutParams
                layoutParams.height = playerViewHeight
                cv_player_view.layoutParams = layoutParams
                cl_content_gig_activation.visible()
                tb_gig_activation.visible()
                sv_gig_activation.post {
                    if (sv_gig_activation != null)
                        sv_gig_activation.fullScroll(ScrollView.FOCUS_UP);
                }



                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE -> {
                Log.d(VideoWithTextFragment.TAG, "LANDSCAPE")

                cv_player_view?.layoutParams?.height = LinearLayout.LayoutParams.MATCH_PARENT
                cv_player_view?.layoutParams?.width = LinearLayout.LayoutParams.MATCH_PARENT
                cl_content_gig_activation.gone()
                tb_gig_activation.gone()


                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
            }
        }
    }


    private fun initObservers() {

        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it ?: "")
        })
        viewModel.observableGigActivation.observe(viewLifecycleOwner, Observer { gigAcivation ->
            if (gigAcivation != null) {
                Glide.with(this).load(gigAcivation.coverImg).placeholder(
                    com.gigforce.app.utils.getCircularProgressDrawable(requireContext())
                ).into(iv_gig_activation)
//                tv_application_gig_activation.text = Html.fromHtml(gigAcivation.subTitle)
                tv_title_toolbar.text = gigAcivation.title
                tv_complete_gig_activation.text = gigAcivation.instruction

                val videoUri = Uri.parse(gigAcivation.videoUrl)
                initializePlayer(videoUri)
                viewModel.updateDraftJpApplication(mJobProfileId, gigAcivation.requiredFeatures)

            }
        })

        viewModel.observableInitApplication.observe(viewLifecycleOwner, Observer {
            if (it == true) {
                pb_gig_activation.gone()
                val jpSettings = viewModel.observableJpApplication.value
                tv_verification_gig_activation.text =
                    jpSettings?.status
                rl_thanks_gig_activation.setBackgroundResource(
                    if (jpSettings?.status == "Draft" || jpSettings?.status == "Applied" || jpSettings?.status == "Inprocess")
                        R.drawable.bg_status_pending else if (jpSettings?.status == "Activated") R.drawable.bg_thanks_for_applying else R.drawable.bg_status_rejected
                )
                ic_applied_gig_activation.setImageResource(
                    if (jpSettings?.status == "Draft" || jpSettings?.status == "Applied" || jpSettings?.status == "Inprocess")
                        R.drawable.ic_status_pending else if (jpSettings?.status == "Activated") R.drawable.ic_applied else R.drawable.ic_status_pending
                )
                tv_application_gig_activation.text = Html.fromHtml(
                    if (jpSettings?.status == "Draft" || jpSettings?.status == "Applied" || jpSettings?.status == "Inprocess")
                        viewModel.observableGigActivation.value?.subTitle + getString(R.string.pending_bold) else if (jpSettings?.status == "Activated") viewModel.observableGigActivation.value?.subTitle + getString(
                        R.string.approved_bold
                    ) else viewModel.observableGigActivation.value?.subTitle + getString(R.string.rejected_bold)
                )
                tv_verification_gig_activation.setCompoundDrawablesWithIntrinsicBounds(
                    if (viewModel.observableJpApplication.value?.status == "Activated") R.drawable.ic_applied else R.drawable.ic_status_pending,
                    0,
                    0,
                    0
                )
                initApplication(viewModel.observableJpApplication.value!!)
            }
        })
        viewModel.getActivationData(mJobProfileId)
    }


    private fun initApplication(jpApplication: JpApplication) {
        adapter.addData(jpApplication.activation)
        adapter.setCallbacks(this)
        for (i in 0 until jpApplication.activation.size) {
            if (!jpApplication.activation[i].isDone) {
                adapter.setImageDrawable(
                    jpApplication.activation[i].type!!,
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_status_pending)!!,
                    false
                )
            } else {
                adapter.setImageDrawable(
                    jpApplication.activation[i].type!!,
                    ContextCompat.getDrawable(requireActivity(), R.drawable.ic_applied)!!,
                    true
                )
            }
        }
        checkForRedirection()

    }


    private fun checkForBackPress() {

        if (navFragmentsData?.getData() != null) {
            if (navFragmentsData?.getData()
                    ?.getBoolean(StringConstants.BACK_PRESSED.value, false) == true
            ) {
                viewModel.redirectToNextStep = false
                navFragmentsData?.setData(bundleOf())
            }
        }
    }


    private val adapter: AdapterGigActivation by lazy {
        AdapterGigActivation()
    }

    private fun setupRecycler() {
        rv_gig_activation.adapter = adapter
        rv_gig_activation.layoutManager =
            LinearLayoutManager(requireContext())

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: ""
            playbackPosition = it.getLong("key_play_back_position")

        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: ""
            playbackPosition = it.getLong("key_play_back_position")

        }
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putLong("key_play_back_position", playbackPosition)


    }

    override fun onItemClick(dependency: Dependency) {
        viewModel.redirectToNextStep = true
        when (dependency.type) {
            "onsite_document" -> {
                if (dependency.isSlotBooked) {
                    val index = adapter.items.indexOf(Dependency(type = "document"))
                    if (index != -1) {
                        navigate(
                            R.id.fragment_doc_sub,
                            bundleOf(
                                StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                                StringConstants.TITLE.value to adapter.items[index].title,
                                StringConstants.TYPE.value to adapter.items[index].docType
                            )
                        )
                    }

                }
            }
            "document" ->
//                navigate(
//                        if (dependency.isSlotBooked) R.id.fragment_doc_sub else R.id.fragment_upload_cert,
//                        bundleOf(
//                                StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
//                                StringConstants.TITLE.value to dependency.title,
//                                StringConstants.TYPE.value to dependency.docType
//                        )
//                )

                navigate(
                    R.id.fragment_doc_sub,
                    bundleOf(
                        StringConstants.JOB_PROFILE_ID.value to mJobProfileId,
                        StringConstants.TITLE.value to dependency.title,
                        StringConstants.TYPE.value to dependency.docType
                    )
                )

            "learning" -> {
                navigate(
                    R.id.learningCourseDetails,
                    bundleOf(
                        LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to dependency.courseId,
                        StringConstants.FROM_CLIENT_ACTIVATON.value to true
                    )
                )
            }
        }
    }

    private fun checkForRedirection() {
        if (!viewModel.redirectToNextStep) return
        for (i in adapter.items.indices) {
            if (!adapter.items[i].isDone) {
                when (adapter.items[i].type) {

                    "learning" ->
                        if (checForOtherIndices(i, adapter.items)) {
                            navigate(
                                R.id.learningCourseDetails,
                                bundleOf(
                                    LearningCourseDetailsFragment.INTENT_EXTRA_COURSE_ID to adapter.items[i].courseId,
                                    StringConstants.FROM_CLIENT_ACTIVATON.value to true
                                )
                            )
                        }
                }


            }

        }


    }

    private fun checForOtherIndices(index: Int, items: List<Dependency>): Boolean {
        var allTrue = true
        for (i in items.indices) {
            if (index != i && !items[i].isDone) {
                allTrue = false
                break
            }
        }
        return allTrue
    }

    private var player: SimpleExoPlayer? = null
    private fun buildMediaSource(uri: Uri): MediaSource {
        val dataSourceFactory = DefaultDataSourceFactory(requireContext(), "gig4ce-agent")
        return ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
    }


    private fun initializePlayer(uri: Uri) {
        if (player == null) {
            player = SimpleExoPlayer.Builder(requireContext()).build()
        }
        player?.addListener(PlayerEventListener())
        playerView.player = player

        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL
        player?.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        val mediaSource = buildMediaSource(uri)
        if (playbackPosition != 0L) {
            player?.seekTo(currentWindow, playbackPosition)
        }
        if (isPlayingVideo == null || isPlayingVideo == true)
            player?.playWhenReady = true
        player?.prepare(mediaSource, false, false)
    }

    inner class PlayerEventListener : Player.EventListener {


        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            super.onPlayerStateChanged(playWhenReady, playbackState)
            if (playerView == null) return
            isPlayingVideo = playWhenReady && playbackState == Player.STATE_READY
            if (playbackState == Player.STATE_IDLE || !playWhenReady) {
                playerView.keepScreenOn = false
            } else playerView.keepScreenOn = playbackState != Player.STATE_ENDED
        }
    }

    private fun releasePlayer() {
        if (player != null && playerView != null) {
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            playWhenReady = player!!.playWhenReady
            player!!.release()
            player = null
        }
    }

    override fun onResume() {
        super.onResume()
        if (isPlayingVideo == true)
            player?.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player?.playWhenReady = false
    }

    override fun onStop() {
        super.onStop()
        if (player != null) {
            playbackPosition = player?.currentPosition ?: 0L
            player?.playWhenReady = false
        }


    }

    override fun onDestroy() {
        super.onDestroy()
        releasePlayer()
    }


}