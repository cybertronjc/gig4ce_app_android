package com.gigforce.app.modules.assessment

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.ItemDecor
import kotlinx.android.synthetic.main.fragment_assessment_result.*
import kotlinx.android.synthetic.main.layout_rv_question_wisr_sum_assess_result.view.*
import kotlinx.android.synthetic.main.toolbar.*

class AssessmentResultFragment : BaseFragment() {
    private var adapter: RecyclerGenericAdapter<Boolean>? = null
    private val viewModelAssessmentResult by lazy {
        ViewModelProvider(this).get(ViewModelAssessmentResult::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_assessment_result, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setupRecycler()
        initObservers()

    }

    private fun initObservers() {

        viewModelAssessmentResult.observableQuestionWiseSumList.observe(viewLifecycleOwner,
            Observer {
                adapter?.addAll(it)
            })
    }

    private fun setupRecycler() {
        adapter = RecyclerGenericAdapter<Boolean>(context,
            PFRecyclerViewAdapter.OnViewHolderClick<Boolean> { view, position, item -> },
            RecyclerGenericAdapter.ItemInterface { obj, viewHolder, position ->

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.text = "" + (position + 1)
                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.isSelected = true
                viewHolder.itemView.fl_rv_question_wise_sum_assess_result.setSolidColor(if (obj) "#ffd9e6" else "#888888")

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.setTextColor(
                    if (obj) activity?.getColor(R.color.darkish_pink_100)!! else activity?.getColor(
                        R.color.black_85
                    )!!
                )
            })
        adapter?.setLayout(R.layout.layout_rv_question_wisr_sum_assess_result)
        rv_question_wise_sum_assess_frag.layoutManager = GridLayoutManager(activity, 5)
        rv_question_wise_sum_assess_frag.addItemDecoration(
            ItemDecor(
                resources.getDimensionPixelSize(
                    R.dimen.size_10
                ),
                5
            )
        )
        rv_question_wise_sum_assess_frag.adapter = adapter
        viewModelAssessmentResult.getQuestionWiseSumData()
        rv_sug_learnings_assess_result.adapter = AdapterSuggestedLearning()
        rv_sug_learnings_assess_result.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rv_sug_learnings_assess_result.addItemDecoration(
            HorizontaltemDecoration(
                activity,
                R.dimen.size_16
            )
        )


    }

    private fun initUI() {
        tv_title_toolbar.text = getString(R.string.assessment)
        tv_score_assess_result.text = Html.fromHtml("You have scored <b>70%</b> in your assessment")
        tv_new_cert_asses_frag.text =
            Html.fromHtml("<u>New certificate has been added to profile .</u>")
    }
}