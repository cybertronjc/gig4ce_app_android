package com.gigforce.learning.assessment

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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.example.learning.R
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.utils.getScreenWidth
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.layout_assessment_dialog.*
import java.text.SimpleDateFormat
import java.util.*


/**
 * @author Rohit Sharma
 * date 19/7/2020
 */
class AssessmentDialog : DialogFragment() {

    private val viewModel: AssessmentDialogViewModel by viewModels()

    private lateinit var mModuleId: String
    private lateinit var mLessonId: String
    private var isPassed: Boolean?=false

    private var assessmentResultWithNextDest: AssessmentResult? = null

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
        initClicks()
        pb_assessment_dialog.visible()

        arguments?.let {
            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let
            isPassed = it.getBoolean(INTENT_EXTRA_ISPASSED) ?: return@let
        }

        initUIAsPerState(arguments?.getInt(StringConstants.ASSESSMENT_DIALOG_STATE.value),isPassed)
        initViewModel()
    }

    private fun initViewModel() {
        viewModel.savingAssessmentState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {


                    }
                    is Lce.Content -> {
                        assessmentResultWithNextDest = it.content
                        when (it.content.state) {
                            STATE_PASS -> statePass()
                            STATE_REAPPEAR -> stateReappear()
                        }

                    }
                    is Lce.Error -> {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.unable_to_save_state))
                            .setMessage(getString(R.string.unable_to_mark_assessment))
                            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
                            .show()
                        pb_assessment_dialog.gone()

                    }
                }
            })
    }

    private fun initUIAsPerState(state: Int?,isPassed:Boolean?) {
        when (state) {
            STATE_PASS -> viewModel.saveAssessmentState(mModuleId, mLessonId, state,isPassed)
            STATE_REAPPEAR -> viewModel.saveAssessmentState(mModuleId, mLessonId, state,isPassed)
            else -> {

                isCancelable = true
                iv_bg_top_assess_dialog.gone()
                parent_assessment_dialog.visible()
                pb_assessment_dialog.gone()
                tv_message_assess_dialog.setTextColor(resources.getColor(R.color.white))
                tv_message_assess_dialog.setBackgroundColor(resources.getColor(R.color.lipstick))
                val lp: ConstraintLayout.LayoutParams =
                    tv_message_assess_dialog.layoutParams as ConstraintLayout.LayoutParams
                lp.setMargins(0, 0, 0, 0)
                tv_message_assess_dialog.setPadding(
                    0,
                    resources.getDimensionPixelSize(R.dimen.size_15),
                    0,
                    resources.getDimensionPixelSize(R.dimen.size_15)
                )
                tv_message_assess_dialog.layoutParams = lp
                tv_do_it_later_assess_dialog.visibility = View.VISIBLE
                tv_message_assess_dialog.text = getString(R.string.good_luck)
                tv_assess_name_assess_dialog.text =
                    "${arguments?.getString(StringConstants.ASSESSMENT_NAME.value)}"

                tv_level_assess_dialog.text =
                    "${getString(R.string.level)} ${arguments?.getInt(StringConstants.LEVEL.value)}"
                tv_action_assess_dialog.text = getString(R.string.start_assessment)

                tv_ques_count_assess_dialog.text =
                    "${getString(R.string.total_questions)} : ${arguments?.getInt(StringConstants.QUESTIONS_COUNT.value)} "
                val dateFormatter = SimpleDateFormat("hh:mm:ss")
                val calInstance = Calendar.getInstance()
                calInstance.time =
                    dateFormatter.parse(arguments?.getString(StringConstants.DURATION.value))

                tv_time_assess_dialog.text =
                    if (calInstance.get(Calendar.HOUR) == 0) "${getString(R.string.time_duration)} : ${
                        calInstance.get(
                            Calendar.MINUTE
                        )
                    } ${
                        getString(
                            R.string.mins
                        )
                    } ${calInstance.get(Calendar.SECOND)} ${getString(R.string.seconds)}" else "${
                        getString(
                            R.string.time_duration
                        )
                    } : ${
                        calInstance.get(
                            Calendar.HOUR
                        )
                    } ${
                        calInstance.get(
                            Calendar.MINUTE
                        )
                    } ${
                        getString(
                            R.string.mins
                        )
                    } ${calInstance.get(Calendar.SECOND)} ${getString(R.string.seconds)}"


            }
        }
    }

    private fun stateReappear() {
        isCancelable = false
        iv_bg_top_assess_dialog.gone()
        parent_assessment_dialog.visible()
        pb_assessment_dialog.gone()
        tv_ques_count_assess_dialog.visibility = View.GONE
        tv_time_assess_dialog.visibility = View.GONE
        tv_assessment_result__assess_dialog.visibility = View.VISIBLE
        val builder = SpannableStringBuilder()
        val spannableString = SpannableString(
            "${getString(R.string.u_attempt)} ${arguments?.getInt(StringConstants.QUESTIONS_COUNT.value)} ${
                getString(
                    R.string.questions
                )
            }"
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            spannableString.indexOf(getString(R.string.attempt)) + 7,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        builder.append(spannableString)
        val spanable2 = SpannableString(
            " ${getString(R.string.and_from)} ${getString(R.string.that)}  ${
                arguments?.getInt(
                    StringConstants.RIGHT_ANSWERS.value
                )
            } ${getString(R.string.answer_is_correct)}"
        )
        spanable2.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            spanable2.indexOf(getString(R.string.that)) + 4,
            spanable2.indexOf(getString(R.string.is_string)),
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        builder.append(spanable2)
        tv_assessment_result__assess_dialog.setText(builder, TextView.BufferType.SPANNABLE)
        tv_message_assess_dialog.text = getString(R.string.oops)
        tv_assess_name_assess_dialog.text = getString(R.string.assessment_not_completed)
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
        parent_assessment_dialog.visible()
        pb_assessment_dialog.gone()
        isCancelable = false
        tv_ques_count_assess_dialog.visibility = View.GONE
        tv_time_assess_dialog.visibility = View.GONE
        tv_assessment_result__assess_dialog.visibility = View.VISIBLE
        val builder = SpannableStringBuilder()

        val spannableString = SpannableString(
            "${getString(R.string.u_attempt)} ${arguments?.getInt(StringConstants.QUESTIONS_COUNT.value)} ${
                getString(
                    R.string.questions
                )
            }"
        )
        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            spannableString.indexOf(getString(R.string.attempt)) + 7,
            spannableString.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )


        builder.append(spannableString)
        val spanable2 = SpannableString(
            " ${getString(R.string.and_from)} ${getString(R.string.that)}  ${
                arguments?.getInt(
                    StringConstants.RIGHT_ANSWERS.value
                )
            } ${getString(R.string.answer_is_correct)}"
        )
        spanable2.setSpan(
            ForegroundColorSpan(Color.parseColor("#e94b81")),
            spanable2.indexOf(getString(R.string.that)) + 4,
            spanable2.indexOf(getString(R.string.is_string)),
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
                    )!!,
                    viewModel.nextDest?.id,
                    arguments?.getBoolean(
                        INTENT_EXTRA_ISPASSED,
                        false
                    )
                )

            })
        tv_do_it_later_assess_dialog.setOnClickListener {
            dismiss()
            assessmentDialogCallbacks?.doItLaterPressed()
        }
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

        fun newInstance(moduleId: String, lessonId: String, state: Int,isPassed:Boolean?): AssessmentDialog {
            //Setting Dialog State Before Initializing the dialog object
            val bundle = Bundle()
            bundle.putInt(StringConstants.ASSESSMENT_DIALOG_STATE.value, state)
            bundle.putString(INTENT_EXTRA_MODULE_ID, moduleId)
            bundle.putString(INTENT_EXTRA_LESSON_ID, lessonId)
            isPassed?.let {
                bundle.putBoolean(INTENT_EXTRA_ISPASSED,it)
            }
            val assessmentDialog = AssessmentDialog()
            assessmentDialog.arguments = bundle
            return assessmentDialog
        }

        const val STATE_INIT = 1;
        const val STATE_PASS = 2;
        const val STATE_REAPPEAR = 3;

        const val INTENT_EXTRA_MODULE_ID = "module_id"
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
        const val INTENT_EXTRA_ISPASSED = "is_passed"
    }

    interface AssessmentDialogCallbacks {
        fun assessmentState(state: Int, nextLesson: String?,isPassed: Boolean?)
        fun doItLaterPressed()


    }

}