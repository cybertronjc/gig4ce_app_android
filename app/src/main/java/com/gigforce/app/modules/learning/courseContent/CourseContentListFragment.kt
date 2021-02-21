package com.gigforce.app.modules.learning.courseContent

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.assessment.AssessmentFragment
import com.gigforce.app.modules.learning.courseDetails.CourseDetailsViewModel
import com.gigforce.app.modules.learning.courseDetails.LearningDetailsLessonsAdapter
import com.gigforce.app.modules.learning.learningVideo.PlayVideoDialogFragment
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.modules.learning.slides.SlidesFragment
import com.gigforce.app.utils.Lce
import com.gigforce.core.navigation.INavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_course_content_list.*
import javax.inject.Inject

@AndroidEntryPoint
class CourseContentListFragment : Fragment() {

    private lateinit var mCourseId: String
    private lateinit var mModuleId: String

    private val viewModel: CourseDetailsViewModel by viewModels()

    @Inject lateinit var navigation:INavigation

    private val mAdapter: LearningDetailsLessonsAdapter by lazy {
        LearningDetailsLessonsAdapter(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_course_content_list, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {
            mCourseId = it.getString(INTENT_EXTRA_COURSE_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        arguments?.let {
            mCourseId = it.getString(INTENT_EXTRA_COURSE_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
        }

        initView()
        initViewModel()
    }

    private fun initView() {
        toolbar.setOnClickListener {
            activity?.onBackPressed()
        }

        learningAndAssessmentRV.layoutManager = LinearLayoutManager(
            requireContext(),
            RecyclerView.VERTICAL,
            false
        )

        mAdapter.setOnLearningVideoActionListener {

            when (it.type) {
                CourseContent.TYPE_ASSESSMENT -> {
                    navigation.navigateTo(
                        "assessment", bundleOf(
                            AssessmentFragment.INTENT_LESSON_ID to it.id,
                            AssessmentFragment.INTENT_MODULE_ID to it.moduleId
                        )
                    )
                }
                CourseContent.TYPE_SLIDE -> {
                    //todo: register: slides: R.id.slidesFragment
                    navigation.navigateTo(
                        "slides",
                        bundleOf(
                            SlidesFragment.INTENT_EXTRA_SLIDE_TITLE to it.title,
                            SlidesFragment.INTENT_EXTRA_MODULE_ID to it.moduleId,
                            SlidesFragment.INTENT_EXTRA_LESSON_ID to it.id
                        )
                    )
                }
                CourseContent.TYPE_VIDEO -> {
                    PlayVideoDialogFragment.launch(
                        childFragmentManager = childFragmentManager,
                        moduleId = it.moduleId,
                        lessonId = it.id,
                        shouldShowFeedbackDialog = it.shouldShowFeedbackDialog
                    )
                }
                else -> {
                }
            }
        }


        learningAndAssessmentRV.adapter = mAdapter
    }

    private fun initViewModel() {

        viewModel
            .courseLessons
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showLessonsAsLoading()
                    is Lce.Content -> showLessonsOnView(it.content)
                    is Lce.Error -> showErrorInLoadingLessons(it.error)
                }
            })

        viewModel.getCourseLessonsAndAssessments(
            courseId = mCourseId,
            moduleId = mModuleId
        )
    }

    private fun showLessonsAsLoading() {

        learningAndAssessmentRV.gone()
        learning_lessons_learning_error.gone()
        learning_lessons_progress_bar.visible()
    }

    private fun showLessonsOnView(content: List<CourseContent>) {

        learning_lessons_learning_error.gone()
        learning_lessons_progress_bar.gone()
        learningAndAssessmentRV.visible()

        mAdapter.updateCourseContent(content)
    }

    private fun showErrorInLoadingLessons(error: String) {

        learningAndAssessmentRV.gone()
        learning_lessons_progress_bar.gone()

        learning_lessons_learning_error.visible()
        learning_lessons_learning_error.text = error
    }

    companion object {

        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }
}