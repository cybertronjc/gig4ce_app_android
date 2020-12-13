package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.JpSettings
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ApplicationClientActivationViewModel : ViewModel() {
    var profileAvatarName: String = "avatar.jpg"

    val repository = ApplicationClientActivationRepository()

    var redirectToNextStep: Boolean = false

    private val _observableWorkOrderDependency: SingleLiveEvent<JpSettings> by lazy {
        SingleLiveEvent<JpSettings>();
    }
    val observableWorkOrderDependency: SingleLiveEvent<JpSettings> get() = _observableWorkOrderDependency

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableApplicationStatus: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableApplicationStatus: SingleLiveEvent<Boolean> get() = _observableApplicationStatus

    private val _observableInitApplication: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableInitApplication: SingleLiveEvent<Boolean> get() = _observableInitApplication


    private val _observableJpApplication: MutableLiveData<JpApplication> = MutableLiveData()
    val observableJpApplication: MutableLiveData<JpApplication> = _observableJpApplication

    fun getWorkOrderDependency(workOrderId: String) = viewModelScope.launch {
        val item = getJpSettings(workOrderId)
        if (item != null)
            observableWorkOrderDependency.value = item
    }

    suspend fun getJpSettings(workOrderID: String): JpSettings? {

        val items = repository.db.collection("JP_Settings").whereEqualTo("type", "dependency")
            .whereEqualTo("jobProfileId", workOrderID).get().await()
        if (items.documents.isNullOrEmpty()) {
            return null
        }
        return items.toObjects(JpSettings::class.java).first()

    }


    suspend fun getVerification(): VerificationBaseModel? {

        return repository.db.collection("Verification").document(repository.getUID()).get().await()
            .toObject(VerificationBaseModel::class.java)

    }


    fun updateDraftJpApplication(mWorkOrderID: String, dependency: List<Dependency>) =
        viewModelScope.launch {


            val model = getJPApplication(mWorkOrderID)


            if (model.draft.isNullOrEmpty()) {
                model.draft = dependency.toMutableList()

            }
            model.draft.forEach {
                if (!it.isDone || it.refresh) {
                    when (it.type) {
                        "profile_pic" -> {
                            val profileModel = getProfile()
                            profileAvatarName = profileModel.profileAvatarName
                            it.isDone =
                                !profileModel.profileAvatarName.isNullOrEmpty() && profileModel.profileAvatarName != "avatar.jpg"

                        }
                        "about_me" -> {
                            val profileModel = getProfile()
                            it.isDone = !profileModel.aboutMe.isNullOrEmpty()

                        }
                        "driving_licence" -> {
                            val verification = getVerification()
                            it.isDone =
                                verification?.driving_license != null && (verification.driving_license?.state
                                    ?: -2) >= -1
                        }
                        "questionnaire" -> {
                            it.isDone =
                                checkForQuestionnaire(
                                    mWorkOrderID, it.type ?: "", it.title
                                        ?: ""
                                )
                        }
                        "learning" -> {
                            it.isDone = checkIfCourseCompleted(it.moduleId)
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
                repository.db.collection("JP_Applications").document(model.id).set(model)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            observableJpApplication.value = model
                            observableInitApplication.value = true

                        }
                    }
            }


        }


    fun apply(mWorkOrderID: String) = viewModelScope.launch {

        val application = getJPApplication(mWorkOrderID)
        repository.db.collection("JP_Applications").document(application.id)
            .update(
                mapOf(
                    "stepsTotal" to (observableWorkOrderDependency.value?.step ?: 0),
                    "stepDone" to observableWorkOrderDependency.value?.step,
                    "status" to "Applied"

                )
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    observableApplicationStatus.value = true
                }
            }
    }
    fun draftApplication(mWorkOrderID: String)= viewModelScope.launch {
        val application = getJPApplication(mWorkOrderID)
        if(application.status == ""){
            repository.db.collection("JP_Applications").document(application.id)
                .update(mapOf("status" to "Draft"
                ))
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        observableApplicationStatus.value = true
                    }
                }
        }
    }


    suspend fun getJPApplication(workOrderID: String): JpApplication {
        try {
            val items =
                repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
                    .whereEqualTo("gigerId", repository.getUID()).get()
                    .await()

            if (items.documents.isNullOrEmpty()) {
                return JpApplication(JPId = workOrderID, gigerId = repository.getUID())
            }
            val toObject = items.toObjects(JpApplication::class.java).get(0)
            toObject.id = items.documents[0].id
            return toObject
        } catch (e: Exception) {
            _observableError.value = e.message;
        }
        return JpApplication()


    }

    suspend fun checkForQuestionnaire(workOrderID: String, type: String, title: String): Boolean {

        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
            .whereEqualTo("gigerId", repository.getUID()).get()
            .await()
        if (items.documents.isNullOrEmpty()) {
            return false
        }
        val documentSnapshot = items.documents[0]
        return !repository.db.collection("JP_Applications").document(documentSnapshot.id)
                .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await().documents.isNullOrEmpty()


    }


    suspend fun getProfile(): ProfileData {
        return repository.db.collection("Profiles").document(repository.getUID()).get().await()
                .toObject(ProfileData::class.java)!!

    }


    suspend fun checkIfCourseCompleted(moduleId: String): Boolean {
        try{
        val data = repository.db.collection("Course_Progress").whereEqualTo("uid", repository.getUID()).whereEqualTo("type", "module").whereEqualTo("module_id", moduleId).get().await()
        if (data.documents.isNullOrEmpty()) {
            return false
        }
        var allCourseProgress = data.toObjects(GigActivationViewModel.CourseProgress::class.java).get(0)
        allCourseProgress.lesson_progress.let {
            var completed = true
            for (lesson in it) {
                if (lesson.lessonType == "assessment" && !lesson.completed) {
                    return false
                }
            }
            return completed
        }
    } catch (e: java.lang.Exception) {
        _observableError.value = e.message
        return false
    }


    return false
    }


    var userProfileData: SingleLiveEvent<ProfileData> = SingleLiveEvent<ProfileData>()


}
