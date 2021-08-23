package com.gigforce.learning.learning.learningVideo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.learning.R
import com.gigforce.core.datamodels.learning.CourseContent
import com.gigforce.learning.learning.slides.SlidesFragment
import com.gigforce.core.StringConstants
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_rate_lesson.*
import kotlinx.android.synthetic.main.fragment_rate_lesson_main.*
import javax.inject.Inject

interface RateLessonDialogFragmentClosingListener{
    fun rateLessonDialogDismissed()
}

@AndroidEntryPoint
class RateLessonDialogFragment : DialogFragment() {

    private val viewModel: CourseVideoViewModel by viewModels()

    private lateinit var mLessonId: String
    private lateinit var mModuleId: String

    private var nextLessonContent : CourseContent? = null

    @Inject
    lateinit var navigation : INavigation

    private var rateLessonDialogFragmentClosingListener: RateLessonDialogFragmentClosingListener? = null

    fun setRateLessonDialogFragmentClosingListener(rateLessonDialogFragmentClosingListener: RateLessonDialogFragmentClosingListener){
        this.rateLessonDialogFragmentClosingListener = rateLessonDialogFragmentClosingListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        savedInstanceState?.let {

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        arguments?.let {

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        return inflater.inflate(R.layout.fragment_rate_lesson, container, false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putString(INTENT_EXTRA_LESSON_ID, mLessonId)
        outState.putString(INTENT_EXTRA_MODULE_ID, mModuleId)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initViewModel()
        viewModel.markVideoAsComplete(mModuleId, mLessonId)
    }

    private fun initView() {
        submitBtn.setOnClickListener {

            val rating = ratingBar.rating

            if(rating == 0.0f ){
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.alert))
                    .setMessage(getString(R.string.provide_lession_rating))
                    .setPositiveButton(getString(R.string.okay)){_,_ -> }
                    .show()

                return@setOnClickListener
            }

            val explanation = if (explanation_chip_group.checkedChipId == -1) {
                null
            }else{
                explanation_chip_group.checkedChipId == R.id.explanation_yes_chip
            }

            val completeness = if (completeness_chip_group.checkedChipId == -1) {
                null
            }else{
                completeness_chip_group.checkedChipId == R.id.completeness_yes_chip
            }

            val easyToUnderstand = if (easy_to_understand_chip_group.checkedChipId == -1) {
                null
            }else{
                easy_to_understand_chip_group.checkedChipId == R.id.easy_to_understand_yes_chip
            }

            val videoQuality = if (video_quality_chip_group.checkedChipId == -1) {
                null
            }else{
                video_quality_chip_group.checkedChipId == R.id.video_quality_yes_chip
            }

            viewModel.saveVideoFeedback(
                lessonId = mLessonId,
                lessonRating = rating,
                explanation = explanation,
                completeness = completeness,
                easyToUnderStand = easyToUnderstand,
                videoQuality = videoQuality
            )
        }

        skipBtn.setOnClickListener {
            openNextLesson()
        }
    }

    private fun initViewModel() {
        viewModel.videoSaveState
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showLoadingLayout()
                    is Lce.Content -> {
                        //Using viewModel.openNextDestination for result
                    }
                    is Lce.Error -> showErrorLayout(it.error)
                }
            })

        viewModel.openNextDestination.observe(viewLifecycleOwner, Observer { cc ->
            showMainLayout()
            nextLessonContent = cc
        })

        viewModel.saveLessonFeedbackState
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lse.Loading -> showLoadingLayout()
                    Lse.Success -> openNextLesson()
                    is Lse.Error -> showErrorLayout(it.error)
                }
            })
    }

    private fun openNextLesson() {

        dismiss()
        rateLessonDialogFragmentClosingListener?.rateLessonDialogDismissed()

        when (nextLessonContent?.type) {
            CourseContent.TYPE_VIDEO -> {
                PlayVideoDialogFragment.launch(
                    childFragmentManager = childFragmentManager,
                    moduleId = nextLessonContent!!.moduleId,
                    lessonId = nextLessonContent!!.id,
                    shouldShowFeedbackDialog = nextLessonContent!!.shouldShowFeedbackDialog
                )
            }
            CourseContent.TYPE_ASSESSMENT -> {
                //R.id.assessment_fragment
                navigation.navigateTo(
                    "learning/assessment", bundleOf(
                        StringConstants.INTENT_LESSON_ID.value to nextLessonContent!!.id,
                        StringConstants.INTENT_MODULE_ID.value to nextLessonContent!!.moduleId
                    )
                )
            }
            CourseContent.TYPE_SLIDE -> {
                //R.id.slidesFragment
                navigation.navigateTo(
                    "learning/assessmentslides",
                    bundleOf(
                        SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to nextLessonContent!!.title,
                        SlidesFragment.INTENT_EXTRA_MODULE_ID to nextLessonContent!!.moduleId,
                        SlidesFragment.INTENT_EXTRA_LESSON_ID to nextLessonContent!!.id
                    )
                )
            }
            else -> {
                clearBackStackToContentList()
                dismiss()
            }
        }
    }

    private fun clearBackStackToContentList() {
        try {
            navigation.getBackStackEntry("learning/assessmentListFragment")
            navigation.popBackStack("learning/assessmentListFragment", false)
        } catch (e: Exception) {

            try {
                navigation.getBackStackEntry("learning/courseContentListFragment")
                navigation.popBackStack("learning/courseContentListFragment", false)
            } catch (e: Exception) {

                try {
                    navigation.getBackStackEntry("learning/coursedetails")
                    navigation.popBackStack("learning/coursedetails", false)
                } catch (e: Exception) {

                    try {
                        navigation.getBackStackEntry("learning/main")
                        navigation.popBackStack("learning/main", false)
                    } catch (e: Exception) {

                    }
                }
            }
        }
    }

    private fun showErrorLayout(error: String) {
        fragment_rate_lesson_progress_bar.gone()
        fragment_rate_lesson_main_layout.gone()

        fragment_rate_lesson_error.visible()
        fragment_rate_lesson_error.text = error
    }

    private fun showMainLayout() {
        fragment_rate_lesson_progress_bar.gone()
        fragment_rate_lesson_error.gone()
        fragment_rate_lesson_main_layout.visible()

    }

    private fun showLoadingLayout() {
        fragment_rate_lesson_main_layout.gone()
        fragment_rate_lesson_error.gone()
        fragment_rate_lesson_progress_bar.visible()
    }


    companion object {
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
        const val TAG = "RateLessonDialogFragment"

        fun launch(
            childFragmentManager: FragmentManager,
            rateLessonDialogFragmentClosingListener: RateLessonDialogFragmentClosingListener,
            moduleId: String,
            lessonId: String
        ) {
            val frag = RateLessonDialogFragment()
            val bundle = bundleOf(
                INTENT_EXTRA_MODULE_ID to moduleId,
                INTENT_EXTRA_LESSON_ID to lessonId
            )

            frag.arguments = bundle
            frag.setRateLessonDialogFragmentClosingListener(rateLessonDialogFragmentClosingListener)
            frag.show(childFragmentManager, TAG)
        }
    }


}