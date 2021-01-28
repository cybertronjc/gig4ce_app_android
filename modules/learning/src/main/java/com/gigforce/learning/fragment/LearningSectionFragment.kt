package com.gigforce.learning.fragment

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.example.learning.R
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM
import com.gigforce.common_ui.viewdatamodels.FeatureLayoutDVM
import com.gigforce.core.MyFragment
import com.gigforce.core.navigation.INavigation
import com.gigforce.learning.di.ILearningModuleComponentProvider
import com.gigforce.learning.viewmodel.LearningSectionFragmentViewModel
import kotlinx.android.synthetic.main.learning_section_fragment_fragment.*
import javax.inject.Inject

class LearningSectionFragment : MyFragment() {

    private val viewModel: LearningSectionFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.learning_section_fragment_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        learning_component.bind(FeatureLayoutDVM(R.drawable.learning_icon, "Learning", getFeaturedItems()))
    }

    private fun getFeaturedItems(): List<Any> {
        val featureItems = ArrayList<Any>()
        featureItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fcourse_behavioural_skills.jpg?alt=media&token=b6d54c0a-0e9a-4f0d-8134-83b96c8aa64a",
                "Behavioral Skills",
                "Level 1"
            )
        )

        return featureItems
    }
}