package com.gigforce.app.modules.learning.slides.types

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_learning_slide_completed.*

class SlidesCompletedFragment : BaseFragment() {

    companion object {
        const val TAG = "SlidesCompletedFragment"

        fun getInstance() : SlidesCompletedFragment{
            return SlidesCompletedFragment()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflateView(R.layout.fragment_learning_slide_completed, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        go_back_btn.setOnClickListener {
            activity?.onBackPressed()
        }
    }
}