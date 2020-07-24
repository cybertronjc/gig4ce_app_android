package com.gigforce.app.modules.assessment

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.utils.widgets.CustomScrollView

class ViewModelAssessmentFragment : ViewModel() {


    internal val observableDialogResult: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    internal val observableDialogInit: MutableLiveData<Nothing> by lazy {
        MutableLiveData<Nothing>();
    }


    fun isQuestionVisible(view: View, scrollView: CustomScrollView): Boolean {
        val scrollBounds = Rect()
        scrollView.getDrawingRect(scrollBounds)
        val top = view.y
        val bottom = top + view.height
        return scrollBounds.top < top && scrollBounds.bottom > bottom
    }

    fun switchAsPerState(state: Int) {
        when (state) {
            AssessmentDialog.STATE_INIT -> observableDialogInit.value = null
            AssessmentDialog.STATE_PASS -> observableDialogResult.value = true
            AssessmentDialog.STATE_REAPPEAR -> observableDialogResult.value = false
        }
    }

}