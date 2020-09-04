package com.gigforce.app.modules.assessment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.assessment.models.Assessment
import com.gigforce.app.modules.learning.LearningViewModel
import com.gigforce.app.modules.learning.courseDetails.CourseDetailsViewModel
import com.gigforce.app.modules.learning.courseDetails.LearningCourseDetailsFragment
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.app.utils.Lce
import kotlinx.android.synthetic.main.fragment_assessment_list.*


class AssessmentListFragment : BaseFragment() {

    private lateinit var mCourseId: String
    private lateinit var mModuleId: String

    private val viewModel : CourseDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_assessment_list, inflater, container)

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

        initViewModel()
    }

    private fun initViewModel() {
        viewModel.courseAssessments
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showAssessmentsAsLoading()
                    is Lce.Content -> showAssessments(it.content)
                    is Lce.Error -> showAssessmentsAsError(it.error)
                }
            })

        viewModel.getCourseAssessments(mCourseId,mModuleId)
    }

    private fun showAssessmentsAsError(error: String) {
        assessment_list_progress_bar.gone()
        assessmentListRV.gone()
        asssessment_error.visible()

        asssessment_error.text = error
    }

    private fun showAssessmentsAsLoading() {
        asssessment_error.gone()
        assessmentListRV.gone()
        assessment_list_progress_bar.visible()
    }

    private fun showAssessments(assessments: List<CourseContent>) {

        asssessment_error.gone()
        assessment_list_progress_bar.gone()

        if(assessments.isEmpty()){
            asssessment_error.visible()
            asssessment_error.text = "No Assessment Found"
        }else {

            assessmentListRV.visible()
            assessmentListRV.layoutManager = LinearLayoutManager(requireContext())
            val adapter = AssessmentListAdapter(
                resources,
                assessments
            )
            assessmentListRV.adapter = adapter
        }
    }

    companion object{

        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }

}