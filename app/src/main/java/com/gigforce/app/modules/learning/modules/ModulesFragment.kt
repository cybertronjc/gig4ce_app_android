package com.gigforce.app.modules.learning.modules

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.learning.learningVideo.LearningVideo
import com.gigforce.app.modules.learning.learningVideo.LearningVideoLineAdapter
import kotlinx.android.synthetic.main.fragment_course_content_list.*

/*
class ModulesFragment : Fragment() {

    private lateinit var mAdapter: LearningVideoLineAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    val videoList = listOf(
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Getting prepared for a product demo",
            videoLength = "4 Slides",
            lessonName = "Lesson 1",
            lessonsSeeMoreButton = "Replay"
        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Conducting an effective product demo",
            videoLength = "60 Sec",
            lessonName = "Lesson 2",
            lessonsSeeMoreButton = "Replay"

        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Post demo: How to connect with customer",
            videoLength = "01:34",
            lessonName = "Lesson 3",
            lessonsSeeMoreButton = "Play Now"
        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Conducting an effective product demo",
            videoLength = "60 Sec",
            lessonName = "Lesson 2",
            lessonsSeeMoreButton = "Replay"

        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Conducting an effective product demo",
            videoLength = "60 Sec",
            lessonName = "Lesson 2",
            lessonsSeeMoreButton = "Replay"

        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_course_content_list, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        learningAndAssessmentRV.layoutManager = mLayoutManager
        mAdapter = LearningVideoLineAdapter(videoList)
        mAdapter.setOnLearningVideoActionListener {

            if (it == 0)
                navigate(R.id.slidesFragment)
            else
                navigate(R.id.playVideoDialogFragment)
        }
        learningAndAssessmentRV.adapter = mAdapter
    }

}*/
