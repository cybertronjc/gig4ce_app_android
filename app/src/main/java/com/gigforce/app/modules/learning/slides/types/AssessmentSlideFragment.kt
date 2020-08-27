package com.gigforce.app.modules.learning.slides.types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_learning_slide_assessment.*

class AssessmentSlideFragment : BaseFragment() {

    companion object {
        const val TAG = "AssessmentFragment"

        private const val KEY_LESSON_ID = "lesson_id"
        private const val KEY_ASSESSMENT_TITLE = "assessment_title"
        private const val KEY_ASSESSMENT_DESCRIPTION = "assessment_description"

        fun getInstance(
            assessmentId: String,
            assessmentTitle: String,
            assessmentDescription: String
        ): AssessmentSlideFragment {
            return AssessmentSlideFragment().apply {
                arguments = bundleOf(
                    KEY_LESSON_ID to assessmentId,
                    KEY_ASSESSMENT_TITLE to assessmentTitle,
                    KEY_ASSESSMENT_DESCRIPTION to assessmentDescription
                )
            }
        }
    }

    private lateinit var mAssessmentId: String
    private lateinit var mAssessmentTitle: String
    private lateinit var mAssessmentDescription: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflateView(R.layout.fragment_learning_slide_assessment, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {

            mAssessmentId = it.getString(KEY_LESSON_ID) ?: return@let
            mAssessmentTitle = it.getString(KEY_ASSESSMENT_TITLE) ?: return@let
            mAssessmentDescription = it.getString(KEY_ASSESSMENT_DESCRIPTION) ?: return@let
        }

        savedInstanceState?.let {

            mAssessmentId = it.getString(KEY_LESSON_ID) ?: return@let
            mAssessmentTitle = it.getString(KEY_ASSESSMENT_TITLE) ?: return@let
            mAssessmentDescription = it.getString(KEY_ASSESSMENT_DESCRIPTION) ?: return@let
        }

        setAssessmentInfoOnView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {

            putString(KEY_LESSON_ID, mAssessmentId)
            putString(KEY_ASSESSMENT_TITLE, mAssessmentTitle)
            putString(KEY_ASSESSMENT_DESCRIPTION, mAssessmentDescription)
        }
    }

    private fun setAssessmentInfoOnView() {
        assessment_title_tv.text = mAssessmentTitle
        assessment_desc_tv.text = mAssessmentDescription
    }
}