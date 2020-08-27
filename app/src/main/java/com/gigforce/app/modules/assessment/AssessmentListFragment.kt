package com.gigforce.app.modules.assessment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.assessment.models.Assessment
import kotlinx.android.synthetic.main.fragment_assessment_list.*


class AssessmentListFragment : BaseFragment() {

    val assessmentList = listOf(
        Assessment(
            id = "01",
            status = Assessment.STATU_COMPLETED,
            title = "Assessment 1",
            assessmentLength = "02:00"
        ),
        Assessment(
            id = "02",
            status = Assessment.STATUS_PENDING,
            title = "Assessment 3",
            assessmentLength = "01:00"
        ),
        Assessment(
            id = "03",
            status = Assessment.STATUS_PENDING,
            title = "Assessment 3",
            assessmentLength = "04:00"
        ),
        Assessment(
            id = "04",
            status = Assessment.STATUS_PENDING,
            title = "Assessment 4",
            assessmentLength = "05:00"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_assessment_list, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assessmentListRV.layoutManager = LinearLayoutManager(requireContext())
        val adapter = AssessmentListAdapter(
            resources,
            assessmentList
        )

        assessmentListRV.adapter = adapter
    }

}