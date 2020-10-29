package com.gigforce.app.modules.learning.learningVideo


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.Lse
import kotlinx.android.synthetic.main.fragment_rate_lesson.*
import kotlinx.android.synthetic.main.fragment_rate_lesson_main.*

interface RateLessonDialogFragmentClosingListener{

    fun rateLessonDialogDismissed()
}

class RateLessonDialogFragment : DialogFragment() {

    private val viewModel: CourseVideoViewModel by viewModels()

    private lateinit var mLessonId: String
    private lateinit var mModuleId: String

    private var nextLessonContent : CourseContent? = null

    private val navigationController: NavController by lazy {
        requireActivity().findNavController(R.id.nav_fragment)
    }

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
            val explanation = if (explanation_chip_group.checkedChipId == -1) {
                null
            }else{
                explanation_chip_group.checkedChipId == R.id.explanation_yes_chip
            }

            val relevance = if (relevance_chip_group.checkedChipId == -1) {
                null
            }else{
                relevance_chip_group.checkedChipId == R.id.relevance_yes_chip
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
                relevance = relevance,
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
                    lessonId = nextLessonContent!!.id
                )
            }
            CourseContent.TYPE_ASSESSMENT -> {
                navigationController.navigate(
                    R.id.assessment_fragment, bundleOf(
                        AssessmentFragment.INTENT_LESSON_ID to nextLessonContent!!.id,
                        AssessmentFragment.INTENT_MODULE_ID to nextLessonContent!!.moduleId
                    )
                )
            }
            CourseContent.TYPE_SLIDE -> {
                navigationController.navigate(
                    R.id.slidesFragment,
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