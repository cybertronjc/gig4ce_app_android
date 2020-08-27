package com.gigforce.app.modules.assessment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_assessment_dialog.*


/**
 * @author Rohit Sharma
 * date 19/7/2020
 */
class AssessmentDialog : DialogFragment() {

    private var assessmentDialogCallbacks: AssessmentDialogCallbacks? = null;
    fun setCallbacks(assessmentDialogCallbacks: AssessmentDialogCallbacks) {
        this.assessmentDialogCallbacks = assessmentDialogCallbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_assessment_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks();
        initUIAsPerState(arguments?.getInt(StringConstants.ASSESSMENT_DIALOG_STATE.value))
    }

    private fun initUIAsPerState(state: Int?) {
        when (state) {
            STATE_PASS -> statePass()
            STATE_REAPPEAR -> stateReappear()
        }
    }

    private fun stateReappear() {
        tv_ques_count_assess_dialog.visibility = View.GONE
        tv_time_assess_dialog.visibility = View.GONE
        tv_assessment_result__assess_dialog.visibility = View.VISIBLE
        val builder = SpannableStringBuilder()
        val spannableString = SpannableString(
            "You attempt 10 questions"
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            11,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spannableString)
        val spanable2 = SpannableString(
            " and from \n" +
                    "that 7 answer is correct."
        )
        spanable2.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            15,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spanable2)
        tv_assessment_result__assess_dialog.setText(builder, TextView.BufferType.SPANNABLE)
        tv_message_assess_dialog.text = getString(R.string.oops)
        tv_assess_name_assess_dialog.text = getString(R.string.assess_complete)
        tv_action_assess_dialog.text = getString(R.string.scorecard)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent_access_dialog)
        constraintSet.connect(
            R.id.tv_action_assess_dialog,
            ConstraintSet.TOP,
            R.id.tv_assessment_result__assess_dialog,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.size_25)
        )
        constraintSet.applyTo(parent_access_dialog)

    }

    private fun statePass() {
        tv_ques_count_assess_dialog.visibility = View.GONE
        tv_time_assess_dialog.visibility = View.GONE
        tv_assessment_result__assess_dialog.visibility = View.VISIBLE
        val builder = SpannableStringBuilder()
        val spannableString = SpannableString(
            "You attempt 10 questions"
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            11,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spannableString)
        val spanable2 = SpannableString(
            " and from \n" +
                    "that 7 answer is correct."
        )
        spanable2.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            15,
            24,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spanable2)
        tv_assessment_result__assess_dialog.setText(builder, TextView.BufferType.SPANNABLE)
        tv_message_assess_dialog.text = getString(R.string.congrats)
        tv_assess_name_assess_dialog.text = getString(R.string.assess_complete)
        tv_action_assess_dialog.text = getString(R.string.scorecard)
        val constraintSet = ConstraintSet()
        constraintSet.clone(parent_access_dialog)
        constraintSet.connect(
            R.id.tv_action_assess_dialog,
            ConstraintSet.TOP,
            R.id.tv_assessment_result__assess_dialog,
            ConstraintSet.BOTTOM,
            resources.getDimensionPixelSize(R.dimen.size_25)
        )
        constraintSet.applyTo(parent_access_dialog)

    }

    private fun initClicks() {
        PushDownAnim.setPushDownAnimTo(tv_action_assess_dialog)
            .setOnClickListener(View.OnClickListener {
                dismiss()
                assessmentDialogCallbacks?.assessmentState(
                    arguments?.getInt(
                        StringConstants.ASSESSMENT_DIALOG_STATE.value,
                        0
                    )!!
                )

            })
    }

    override fun onStart() {
        super.onStart()
        dialog?.window
            ?.setLayout(
                (getScreenWidth(requireActivity()).width - resources.getDimension(R.dimen.size_32)).toInt(),
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        dialog?.window
            ?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
    }


    companion object {
        fun newInstance(state: Int): AssessmentDialog {
            //Setting Dialog State Before Initializing the dialog object
            val bundle = Bundle()
            bundle.putInt(StringConstants.ASSESSMENT_DIALOG_STATE.value, state)
            val assessmentDialog = AssessmentDialog()
            assessmentDialog.arguments = bundle
            return assessmentDialog
        }

        const val STATE_INIT = 1;
        const val STATE_PASS = 2;
        const val STATE_REAPPEAR = 3;
    }

    interface AssessmentDialogCallbacks {
        fun assessmentState(state: Int)

    }

}