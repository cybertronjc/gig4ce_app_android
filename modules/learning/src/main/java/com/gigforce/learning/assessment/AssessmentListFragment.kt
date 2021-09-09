package com.gigforce.learning.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.learning.R
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.learning.learning.courseDetails.CourseDetailsViewModel
import com.gigforce.core.datamodels.learning.CourseContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_assessment_list.*
import javax.inject.Inject

@AndroidEntryPoint
class AssessmentListFragment : Fragment(), AssessmentClickListener {

    private lateinit var mCourseId: String
    private lateinit var mModuleId: String

    private val viewModel : CourseDetailsViewModel by viewModels()

    @Inject lateinit var navigation : INavigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_assessment_list, container,false)

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

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
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
            asssessment_error.text = getString(R.string.no_assessment_found_learning)
        }else {

            assessmentListRV.visible()
            assessmentListRV.layoutManager = LinearLayoutManager(requireContext())
            val adapter = AssessmentListAdapter(
                resources,
                assessments.sortedBy { it.priority }
            ).apply {
                setListener(this@AssessmentListFragment)
            }
            assessmentListRV.adapter = adapter
        }
    }

    companion object{

        const val INTENT_EXTRA_COURSE_ID = "course_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }

    override fun onAssessmentClicked(assessment: CourseContent) {
        //if(assessment.completed || assessment.currentlyOnGoing) {
            navigation.navigateTo("learning/assessment",bundleOf(
                AssessmentFragment.INTENT_LESSON_ID to assessment.id,
                AssessmentFragment.INTENT_MODULE_ID to assessment.moduleId
            ))
//        } else{
//            Toast.makeText(
//                requireContext(),
//                "Please complete previous lessons first",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
    }

}