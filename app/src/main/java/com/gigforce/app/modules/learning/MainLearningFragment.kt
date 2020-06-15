package com.gigforce.app.modules.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_main_learning.*
import kotlinx.android.synthetic.main.fragment_main_learning_recent_video_item.view.*


class MainLearningFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_main_learning, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        learningBackButton.setOnClickListener {
            activity?.onBackPressed()
        }

        dummLayout1.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout1.videoDescTV.text = "Industry Based"

        dummLayout2.videoTitleTV.text = "How to apply for driving license?"
        dummLayout2.videoDescTV.text = "Role Based"

        dummLayout3.videoTitleTV.text = "How to acheive your retail goal market?"
        dummLayout3.videoDescTV.text = "Industry Based"

    }

}