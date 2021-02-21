package com.gigforce.app.modules.learning.slides.types

import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.assessment.AssessmentFragment
import com.gigforce.app.modules.learning.models.CourseContent
import com.gigforce.core.navigation.INavigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_learning_slide_assessment.*
import javax.inject.Inject

@AndroidEntryPoint
class AssessmentSlideFragment : Fragment() {

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

    @Inject lateinit var navigation:INavigation

    private lateinit var mAssessmentId: String
    private lateinit var mAssessmentTitle: String
    private lateinit var mAssessmentDescription: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_learning_slide_assessment, container)

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

        start_assessment_btn.setOnClickListener {
            // todo: register: assessment: R.id.assessment_fragment
            navigation.navigateTo("assessment",  bundleOf(
                AssessmentFragment.INTENT_LESSON_ID to mAssessmentId
            ))
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
        assessment_desc_tv.setOnClickListener {
            showText(mAssessmentDescription)
        }

        if (mAssessmentDescription.length >= 180)
            assessment_desc_tv.text = getDescriptionText(mAssessmentDescription.substring(0, 180))
        else
            assessment_desc_tv.text = mAssessmentDescription
    }

    private fun getDescriptionText(text: String): SpannableString {
        if (text.isBlank())
            return SpannableString("")

        val string = SpannableString(text + SingleImageFragment.READ_MORE)

        val colorLipstick = ResourcesCompat.getColor(resources, R.color.white, null)
        string.setSpan(ForegroundColorSpan(colorLipstick), text.length + 3, string.length - 1, 0)
        string.setSpan(UnderlineSpan(), text.length + 2, string.length, 0)

        return string
    }

    private fun showText(text: String) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_show_text, null)
        val textView = dialogView.findViewById<TextView>(R.id.textView)
        textView.text = text

        MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .setPositiveButton(R.string.okay_text) { _, _ -> }
            .show()
    }
}