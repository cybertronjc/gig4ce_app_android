package com.gigforce.app.modules.assessment

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ViewModelAssessmentResult : ViewModel() {
    internal val observableQuestionWiseSumList: MutableLiveData<List<Boolean>> by lazy {
        MutableLiveData<List<Boolean>>();
    }

    fun getQuestionWiseSumData() {
        observableQuestionWiseSumList.value = mockQuestionWiseSumData()

    }

    fun mockQuestionWiseSumData(): List<Boolean> {
        val list = ArrayList<Boolean>()
        list.add(true)
        list.add(false)
        list.add(true)
        list.add(true)
        list.add(true)
        list.add(false)
        list.add(true)
        list.add(true)
        list.add(false)
        list.add(true)
        return list
    }

}