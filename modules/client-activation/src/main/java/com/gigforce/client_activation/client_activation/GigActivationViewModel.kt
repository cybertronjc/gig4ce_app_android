package com.gigforce.client_activation.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.client_activation.client_activation.models.GigActivation
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.datamodels.learning.LessonProgress
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class GigActivationViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val repository = GigActivationRepository()
    var redirectToNextStep: Boolean = true

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>()
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableJpApplication: SingleLiveEvent<JpApplication> by lazy {
        SingleLiveEvent<JpApplication>()
    }
    val observableJpApplication: SingleLiveEvent<JpApplication> get() = _observableJpApplication

    private val _observableInitApplication: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>()
    }
    val observableInitApplication: SingleLiveEvent<Boolean> get() = _observableInitApplication


    private val _observableGigActivation = MutableLiveData<GigActivation>()
    val observableGigActivation: MutableLiveData<GigActivation> = _observableGigActivation

    fun getActivationData(jobProfileID: String) {

        repository.getCollectionReference().whereEqualTo("jobProfileId", jobProfileID)
            .whereEqualTo("type", "activation").addSnapshotListener { success, err ->
                if (err == null) {
                    if (success?.documents?.isNotEmpty() == true) {
                        observableGigActivation.value =
                            success.toObjects(GigActivation::class.java)[0]

                    }
                } else {
                    observableError.value = err.message
                }
            }

    }

    fun getApplication(
        mJobProfileId: String
    ) = viewModelScope.launch {
        val jpApplication = getJPApplication(mJobProfileId)
        if (jpApplication != null) {
            observableJpApplication.value = jpApplication
        } else {
            observableError.value = "Something Went Wrong"
        }


    }

    suspend fun getJPApplication(jobProfileID: String): JpApplication? {
        return try {
            val items =
                repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileID)
                    .whereEqualTo("gigerId", repository.getUID()).get()
                    .await()
            val toObject = items.toObjects(JpApplication::class.java).get(0)
            toObject.id = items.documents[0].id
            toObject
        } catch (e: Exception) {
            observableError.value = e.message
            null
        }

    }

    fun updateDraftJpApplication(mJobProfileId: String, dependency: List<Dependency>) =
        viewModelScope.launch {


            val model = getJPApplication(mJobProfileId)
            if (model == null) {
                observableError.value = "Application Doesn't Exists"
                return@launch
            }

            if (model.activation.isNullOrEmpty()) {
                model.activation = dependency.toMutableList()
                model.activationStart = Date()
            }
            if (model.status != "Submitted") {
                if (model.activation.all { it.isDone }) {
                    model.status = "Submitted"
                    model.activationComplete = Date()
                }
            }


            model.activation.forEach {
                if (!it.isDone) {
                    when (it.type) {

                        "document", "onsite_document" -> {

                            it.isDone = checkForDrivingCertificate(
                                mJobProfileId,
                                it.type ?: "",
                                it.title ?: ""
                            )

                        }

                        "learning" -> {
                            it.isDone = checkIfCourseCompleted(it.moduleId)
                            if (it.isDone) {
                                it.status = ""
                                model.activationLearningCompletionDate = Date()
                            }
                        }

                    }
                }


                if (model.id.isEmpty()) {
                    repository.db.collection("JP_Applications").document().set(model)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {

                                observableJpApplication.value = model
                                observableInitApplication.value = true

                            }
                        }
                } else {
                    model.setUpdatedAtAndBy(uid = FirebaseAuthStateListener.getInstance()
                        .getCurrentSignInUserInfoOrThrow().uid)
                    repository.db.collection("JP_Applications").document(model.id).set(model)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                observableJpApplication.value = model
                                observableInitApplication.value = true

                            }
                        }
                }


            }


        }

    suspend fun checkForDrivingCertificate(
        jobProfileID: String,
        type: String,
        title: String
    ): Boolean {
        try {
            val items =
                repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileID)
                    .whereEqualTo("gigerId", repository.getUID()).get()
                    .await()
            if (items.documents.isNullOrEmpty()) {
                return false
            }
            val documentSnapshot = items.documents[0]
            return !repository.db.collection("JP_Applications").document(documentSnapshot.id)
                .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                    "title", title
                ).whereEqualTo("type", type).get().await().documents.isNullOrEmpty()
        } catch (e: Exception) {
            observableError.value = e.message
            return false
        }


    }


    suspend fun checkIfCourseCompleted(moduleId: String): Boolean {
        try {
            val data =
                repository.db.collection("Course_Progress").whereEqualTo("uid", repository.getUID())
                    .whereEqualTo("type", "module").whereEqualTo("module_id", moduleId).get()
                    .await()
            if (data.documents.isNullOrEmpty()) {
                return false
            }
            val courseProgress = data.toObjects(CourseProgress::class.java).get(0)
            courseProgress.lesson_progress.let {
                val completed = true
                for (lesson in it) {
                    if (lesson.lessonType == "assessment" && !lesson.completed) {
                        return false
                    }
                }
                return completed
            }
        } catch (e: Exception) {
            observableError.value = e.message
            return false
        }

    }

    data class CourseProgress(
        var lesson_progress: ArrayList<LessonProgress> = ArrayList<LessonProgress>()
    )
}