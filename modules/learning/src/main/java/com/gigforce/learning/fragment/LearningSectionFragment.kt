package com.gigforce.learning.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.learning.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemDVM
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.learning.viewmodel.LearningSectionFragmentViewModel
import kotlinx.android.synthetic.main.learning_section_fragment_fragment.*

class LearningSectionFragment : Fragment() {

    private lateinit var viewModel: LearningSectionFragmentViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.learning_section_fragment_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(LearningSectionFragmentViewModel::class.java)
        learning_component.bind(FeatureLayoutDVM(R.drawable.learning_icon, "Learning", getFeaturedItems()))
    }

    private fun getFeaturedItems(): List<Any> {
        var featureItems = ArrayList<Any>()
        featureItems.add(
            FeatureItemDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fcourse_behavioural_skills.jpg?alt=media&token=b6d54c0a-0e9a-4f0d-8134-83b96c8aa64a",
                "Behavioral Skills",
                "Level 1"
            )
        )
        featureItems.add(
            FeatureItemDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fbehave_2a.jpg?alt=media&token=6022178a-87c5-4b18-8091-76b5889ed79f",
                "Behavioral Skills",
                "Level 2"
            )
        )
        return featureItems
    }
}