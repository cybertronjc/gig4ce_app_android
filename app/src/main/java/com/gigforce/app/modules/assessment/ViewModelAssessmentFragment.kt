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
    internal val observableRunSwipeDownAnim: MutableLiveData<Nothing> by lazy {
        MutableLiveData<Nothing>();
    }
    internal val observableShowHideSwipeDownIcon: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>();
    }
    internal val observableShowHideQuestionHeader: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>();
    }


    fun shouldQuestionHeaderBeVisible(view: View, scrollView: CustomScrollView) {
        val scrollBounds = Rect()
        scrollView.getDrawingRect(scrollBounds)
        val top = view.y
        val bottom = top + view.height
        observableShowHideQuestionHeader.value = if (scrollBounds.top < top && scrollBounds.bottom > bottom) View.GONE else View.VISIBLE
    }

    fun switchAsPerState(state: Int) {
        when (state) {
            AssessmentDialog.STATE_INIT -> observableDialogInit.value = null
            AssessmentDialog.STATE_PASS -> observableDialogResult.value = true
            AssessmentDialog.STATE_REAPPEAR -> observableDialogResult.value = false
        }
    }

    fun bottomReached(reached: Boolean) {
        if (!reached) {
            observableRunSwipeDownAnim.value = null
        }
        observableShowHideSwipeDownIcon.value = if (reached) View.GONE else View.VISIBLE
    }

}