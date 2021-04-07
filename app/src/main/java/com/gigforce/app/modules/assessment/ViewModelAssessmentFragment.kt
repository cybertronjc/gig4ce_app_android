package com.gigforce.app.modules.assessment

import android.graphics.Rect
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

data class observableDialogResultWrapper(
    var result: Boolean,
    var nextNextLessonId: String?,
    var isPassed : Boolean?=false
)

class ViewModelAssessmentFragment(private val modelCallbacks: ModelCallbacks) : ViewModel(),
    ModelCallbacks.ModelResponseCallbacks {

    var nextLessonId: String? = null

    internal val observableDialogResult: MutableLiveData<observableDialogResultWrapper> by lazy {
        MutableLiveData<observableDialogResultWrapper>();
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

    fun switchAsPerState(state: Int, nextNextLessonId: String?,isPassed: Boolean?=false) {
        when (state) {
            AssessmentDialog.STATE_INIT -> observableDialogInit.value = null
            AssessmentDialog.STATE_PASS -> observableDialogResult.value =
                observableDialogResultWrapper(true, nextNextLessonId,isPassed)
            AssessmentDialog.STATE_REAPPEAR -> observableDialogResult.value =
                observableDialogResultWrapper(false, nextNextLessonId,isPassed)
        }
    }

    fun bottomReached(reached: Boolean) {
        if (!reached) {
            observableRunSwipeDownAnim.value = null
        }
        observableShowHideSwipeDownIcon.value = if (reached) View.GONE else View.VISIBLE
    }

    fun getQuestionaire(lessonId: String) {
        modelCallbacks.getQuestionaire(lessonId, this)
    }

    fun submitAnswers(id: String?) {

        modelCallbacks.submitAnswers(id!!, observableAssessmentData.value!!, this)
    }

    override fun QuestionairreSuccess(value: QuerySnapshot?, e: FirebaseFirestoreException?) {
        value?.toObjects(AssementQuestionsReponse::class.java)?.let {
            if(it.size>0)
                observableAssessmentData.value = it[0]
        }
//        if (value?.documents?.isNotEmpty() == true)
//            observableAssessmentData.value =
//                value?.toObjects(AssementQuestionsReponse::class.java)!![0]
    }

    override fun submitAnswerSuccess() {
        observableQuizSubmit.value = true
    }

    override fun submitAnswerFailure(err: String) {
        observableError.value = err
    }

}