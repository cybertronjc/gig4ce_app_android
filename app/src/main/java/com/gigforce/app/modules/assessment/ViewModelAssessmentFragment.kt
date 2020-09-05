package com.gigforce.app.modules.assessment

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ViewModelAssessmentFragment(private val modelCallbacks: ModelCallbacks) : ViewModel(),
    ModelCallbacks.ModelResponseCallbacks {


    internal val observableDialogResult: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    internal val observableQuizSubmit: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>();
    }
    internal val observableError: MutableLiveData<String> by lazy {
        MutableLiveData<String>();
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
    internal val observableAssessmentData: MutableLiveData<AssementQuestionsReponse> by lazy {
        MutableLiveData<AssementQuestionsReponse>();
    }

    fun shouldQuestionHeaderBeVisible(top: Float?, bottom: Float?, scrollBounds: Rect) {
        observableShowHideQuestionHeader.value =
            if (scrollBounds.top < top!! && scrollBounds.bottom > bottom!!) View.GONE else View.VISIBLE
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

    fun getQuestionaire(lessonId : String) {
        modelCallbacks.getQuestionaire(lessonId,this)
    }

    fun submitAnswers(id: String?) {

        modelCallbacks.submitAnswers(id!!, observableAssessmentData.value!!, this)
    }

    override fun QuestionairreSuccess(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
        observableAssessmentData.value = value?.toObjects(AssementQuestionsReponse::class.java)!![0]
    }

    override fun submitAnswerSuccess() {
        observableQuizSubmit.value = true
    }

    override fun submitAnswerFailure(err: String) {
        observableError.value = err
    }

}