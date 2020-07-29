package com.gigforce.app.modules.assessment

import android.graphics.Bitmap
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.io.File
import java.io.FileOutputStream


class ViewModelAssessmentResult : ViewModel() {
    internal val observableQuestionWiseSumList: MutableLiveData<List<Boolean>> by lazy {
        MutableLiveData<List<Boolean>>();
    }
    internal val observableIsUserPassed: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>();
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

    fun store(bm: Bitmap?, fileName: String, dirPath: String) {
        val dir = File(dirPath)
        if (!dir.exists()) dir.mkdirs()
        val file = File(dirPath, fileName)
        try {
            val fOut = FileOutputStream(file)
            bm?.compress(Bitmap.CompressFormat.PNG, 85, fOut)
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun checkIfUserPassed(passed: Boolean?) {
        observableIsUserPassed.value = if (passed!!) View.GONE else View.VISIBLE
    }

}