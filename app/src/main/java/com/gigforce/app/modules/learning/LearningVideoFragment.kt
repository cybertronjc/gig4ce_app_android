package com.gigforce.app.modules.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.learning.learningVideo.LearningVideo
import com.gigforce.app.modules.learning.learningVideo.LearningVideoLineAdapter
import kotlinx.android.synthetic.main.assessment_bs_item.view.*
import kotlinx.android.synthetic.main.fragment_learning_video.*
import kotlinx.android.synthetic.main.fragment_main_learning.learningBackButton

class LearningVideoFragment : BaseFragment() {

    private lateinit var mAdapter: LearningVideoLineAdapter
    private lateinit var mLayoutManager: LinearLayoutManager

    val videoList = listOf(
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Getting prepared for a product demo",
            videoLength = "03: 34",
            lessonName = "Lesson 1"
        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Conducting an effective product demo",
            videoLength = "60 Sec",
            lessonName = "Lesson 2"
        ),
        LearningVideo(
            thumbnail = R.drawable.bg_user_learning,
            title = "Post demo: How to connect with customer",
            videoLength = "01:34",
            lessonName = "Lesson 3"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_learning_video, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }
        playFab.setOnClickListener {
            navigate(R.id.playVideoDialogFragment)
        }

        setDataOnLearningVideo()

        assessmentItem1.textView74.text = "Getting prepared for a product demo"
        assessmentItem1.textView76.text = "02:00"
        assessmentItem1.textView75.text = "COMPLETED"
        assessmentItem1.textView75.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.green,
                null
            )
        )
        assessmentItem1.textView73.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.green,
                null
            )
        )


        assessmentItem2.textView74.text = "Conducting an effective product demo"
        assessmentItem2.textView76.text = "05:00"
        assessmentItem2.textView75.text = "PENDING"
        assessmentItem2.textView75.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )
        assessmentItem2.textView73.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )


        assessmentItem3.textView74.text = "How to connect with customer"
        assessmentItem3.textView76.text = "05:00"
        assessmentItem3.textView75.text = "PENDING"
        assessmentItem3.textView75.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )
        assessmentItem3.textView73.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )

        assessmentItem4.textView74.text = "Closing the product demo"
        assessmentItem4.textView76.text = "05:00"
        assessmentItem4.textView75.text = "PENDING"
        assessmentItem4.textView75.setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )
        assessmentItem4.textView73.setCardBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                R.color.yellow,
                null
            )
        )


    }

    private fun setDataOnLearningVideo() {
        mLayoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        lessonsRV.layoutManager = mLayoutManager
        mAdapter = LearningVideoLineAdapter(videoList)
        lessonsRV.adapter = mAdapter
    }

}