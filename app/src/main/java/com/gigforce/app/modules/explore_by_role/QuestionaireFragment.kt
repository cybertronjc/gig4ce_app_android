package com.gigforce.app.modules.explore_by_role

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.app.modules.explore_by_role.models.QuestionnaireResponse
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.ext.showToast
import kotlinx.android.synthetic.main.layout_next_add_profile_segments.view.*
import kotlinx.android.synthetic.main.layout_questionaire_fragment.*
import kotlinx.android.synthetic.main.layout_questionnaire.view.*

class QuestionaireFragment : BaseFragment() {
    private lateinit var win: Window
    private val answersList = mutableListOf<QuestionnaireResponse>()
    val viewModel: QuestionnaireFragmentViewModel by activityViewModels<QuestionnaireFragmentViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_questionaire_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initObservers()
        iv_close_questionaire.setOnClickListener {
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )
            popBackState()
        }
        question_dl.tv_field.text = getString(R.string.have_dl)
        question_bike_car.tv_field.text = getString(R.string.own_a_bike)
        question_ready_to_work.tv_field.text = getString(R.string.ready_to_work_6_days)
        question_ready_to_join.tv_field.text = getString(R.string.ready_to_join_immediately)
        question_ready_to_work_on_field.tv_field.text = getString(R.string.ready_to_wotk_on_field)
        question_dl.cb_yes.setOnClickListener {
            question_dl.cb_no.isChecked = false
        }
        question_dl.cb_no.setOnClickListener {
            question_dl.cb_yes.isChecked = false
        }
        question_bike_car.cb_yes.setOnClickListener {
            question_bike_car.cb_no.isChecked = false
        }
        question_bike_car.cb_no.setOnClickListener {
            question_bike_car.cb_yes.isChecked = false
        }
        question_ready_to_work.cb_yes.setOnClickListener {
            question_ready_to_work.cb_no.isChecked = false
        }
        question_ready_to_work.cb_no.setOnClickListener {
            question_ready_to_work.cb_yes.isChecked = false
        }
        question_ready_to_join.cb_yes.setOnClickListener {
            question_ready_to_join.cb_no.isChecked = false
        }
        question_ready_to_join.cb_no.setOnClickListener {
            question_ready_to_join.cb_yes.isChecked = false
        }
        question_ready_to_work_on_field.cb_yes.setOnClickListener {
            question_ready_to_work_on_field.cb_no.isChecked = false
        }
        question_ready_to_work_on_field.cb_no.setOnClickListener {
            question_ready_to_work_on_field.cb_yes.isChecked = false
        }
        ll_next_questionnaire_fragment.tv_action.setOnClickListener {
            if (validate()) {
                pb_questionnaire.visible()
                viewModel.addQuestionnaire(answersList)

            }


        }
        ll_next_questionnaire_fragment.tv_cancel.text = getString(R.string.skip_client)
        ll_next_questionnaire_fragment.tv_cancel.setOnClickListener {
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.MOVE_TO_NEXT_STEP.value to true,
                    StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value to true
                )
            )
            popBackState()
        }


    }

    private fun initObservers() {

        viewModel.observableSuccess.observe(viewLifecycleOwner, Observer {
            pb_questionnaire.gone()
            if (it == "true") {
                navFragmentsData?.setData(
                    bundleOf(
                        StringConstants.MOVE_TO_NEXT_STEP.value to true,
                        StringConstants.NAVIGATE_TO_MARK_AS_INTERESTED.value to true
                    )
                )
                popBackState()
            } else {
                showToast(it!!)
            }
        })
    }

    private fun validate(): Boolean {
        var formValid = true
        answersList.clear()
        for (i in 0 until ll_questionnaire.childCount - 1) {

            if (!ll_questionnaire.getChildAt(i).ll_questionnaire.cb_yes.isChecked && !ll_questionnaire.getChildAt(
                    i
                ).ll_questionnaire.cb_no.isChecked
            ) {
                ll_questionnaire.getChildAt(i).line_questionnaire.setBackgroundColor(
                    resources.getColor(
                        R.color.red
                    )
                )
                formValid = false
            } else {
                answersList.add(
                    QuestionnaireResponse(
                        ll_questionnaire.getChildAt(i).tv_field.text.toString(),
                        ll_questionnaire.getChildAt(i).cb_yes.isChecked
                    )

                )
                ll_questionnaire.getChildAt(i).line_questionnaire.setBackgroundColor(
                    Color.parseColor(
                        "#68979797"
                    )

                )

            }

        }
        return formValid
    }


    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(requireActivity().getColor(R.color.white))
        }
    }

    override fun onBackPressed(): Boolean {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true,
                StringConstants.MOVE_TO_NEXT_STEP.value to false
            )
        )
        return super.onBackPressed()
    }


    private fun restoreStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onStart() {
        super.onStart()
        makeStatusBarTransparent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBar()
    }

}