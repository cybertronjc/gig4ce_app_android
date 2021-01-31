package com.gigforce.learning.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.common_ui.viewdatamodels.FeatureItemCardDVM

class LearningSectionFragmentViewModel : ViewModel() {

    private var _learningItems: MutableLiveData<List<Any>> = MutableLiveData()

    val learningItems: LiveData<List<Any>>
        get() = _learningItems

    init {
        this._learningItems.value = getFeaturedItems()
    }

    private fun getFeaturedItems(): List<Any> {
        val featureItems = ArrayList<Any>()
        featureItems.add(
            FeatureItemCardDVM(
                "https://firebasestorage.googleapis.com/v0/b/gigforce-dev.appspot.com/o/gig4ce-files-bucket%2Fbehavioral_skills%2Fcourse_behavioural_skills.jpg?alt=media&token=b6d54c0a-0e9a-4f0d-8134-83b96c8aa64a",
                "Behavioral Skills",
                "Level 1",
                "learning/main"
            )
        )
        return featureItems
    }
}