package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.GigActivation
import com.gigforce.app.modules.client_activation.models.JpApplication
import com.gigforce.app.modules.client_activation.models.JpSettings
import com.gigforce.app.modules.gigerVerfication.VerificationBaseModel
import com.gigforce.app.modules.landingscreen.models.Dependency
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ApplicationClientActivationViewModel : ViewModel() {
    var profileAvatarName: String = "avatar.jpg"

    val repository = ApplicationClientActivationRepository()

    var redirectToNextStep: Boolean = false

    private val _observableJobProfile: SingleLiveEvent<JpSettings> by lazy {
        SingleLiveEvent<JpSettings>();
    }
    val observableJobProfile: SingleLiveEvent<JpSettings> get() = _observableJobProfile

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

    fun getJobProfileDependency(jobProfileId: String) = viewModelScope.launch {
        val item = getJpSettings(jobProfileId)
        if (item != null)
            observableJobProfile.value = item
    }

    suspend fun getJpSettings(jobProfileId: String): JpSettings? {
        try {
            val items = repository.db.collection("JP_Settings").whereEqualTo("type", "application")
                .whereEqualTo("jobProfileId", jobProfileId).get().await()
            if (items.documents.isNullOrEmpty()) {
                return null
            }
            return items.toObjects(JpSettings::class.java).first()
        } catch (e: Exception) {
            observableError.value = e.message
            return null
        }

    }


    suspend fun getVerification(): VerificationBaseModel? {
        return try {
            repository.db.collection("Verification").document(repository.getUID()).get()
                .await()
                .toObject(VerificationBaseModel::class.java)
        } catch (e: Exception) {
            observableError.value = e.message
            null
        }


    }


    fun updateDraftJpApplication(mJobProfileId: String, dependency: List<Dependency>) =
        viewModelScope.launch {


            val model = getJPApplication(mJobProfileId)


            if (model.application.isNullOrEmpty()) {
                model.application = dependency.toMutableList()

            }
            model.application.forEach {
                if (!it.isDone || it.refresh) {
                    when (it.type) {
                        "profile_pic" -> {
                            val profileModel = getProfile()
                            profileAvatarName = profileModel?.profileAvatarName ?: ""
                            it.isDone =
                                !profileModel?.profileAvatarName.isNullOrEmpty() && profileModel?.profileAvatarName != "avatar.jpg"

                        }
                        "about_me" -> {
                            val profileModel = getProfile()
                            it.isDone = !profileModel?.aboutMe.isNullOrEmpty()

                        }
                        "driving_licence" -> {
                            val verification = getVerification()
                            it.isDone =
                                verification?.driving_license != null && (verification.driving_license?.state
                                    ?: -2) >= -1 && verification?.driving_license?.backImage != "" && verification?.driving_license?.frontImage != ""
                        }
                        "questionnaire" -> {
                            it.isDone =
                                checkForQuestionnaire(
                                    mJobProfileId, it.type ?: "", it.title
                                        ?: ""
                                )
                        }
                        "learning" -> {

                            it.isDone = checkIfCourseCompleted(it.moduleId)
                            if (it.isDone) {
                                model.applicationLearningCompletionDate = Date()
                            }
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


    fun apply(jobProfileId: String) = viewModelScope.launch {

        val application = getJPApplication(jobProfileId)
        var statusUpdate = mapOf(
                "stepsTotal" to (observableJobProfile.value?.step ?: 0),
                "status" to "Submitted",
                "applicationComplete" to Date()

        )
        if(isActivationScreenFound){
            statusUpdate = mapOf(
                    "stepsTotal" to (observableJobProfile.value?.step ?: 0),
                    "status" to "Inprocess",
                    "applicationComplete" to Date()

            )
        }
        repository.db.collection("JP_Applications").document(application.id)
            .update(
                    statusUpdate
            )
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    observableApplicationStatus.value = true
                } else {
                    observableError.value = it?.exception?.message ?: ""
                }
            }
    }

    private val _observableGigActivation = MutableLiveData<Boolean>()
    val observableGigActivation: MutableLiveData<Boolean> = _observableGigActivation
    var isActivationScreenFound = false
    fun getActivationData(jobProfileID: String) {

        repository.getCollectionReference().whereEqualTo("jobProfileId", jobProfileID)
                .whereEqualTo("type", "activation").addSnapshotListener { success, err ->
                    if (err == null) {
                        if (success?.documents?.isNotEmpty() == true) {
                            observableGigActivation.value =
                                    true

                        }
                    } else {
                        observableError.value = err.message
                    }
                }

    }


    fun draftApplication(jobProfileId: String) = viewModelScope.launch {
        val application = getJPApplication(jobProfileId)
        if (application.status == "") {
            repository.db.collection("JP_Applications").document(application.id)
                .update(
                    mapOf(
                        "status" to "Interested"
                    )
                )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        observableApplicationStatus.value = true
                    } else {
                        observableError.value = it?.exception?.message ?: ""
                    }
                }
        }
    }


    suspend fun getJPApplication(jobProfileId: String): JpApplication {
        try {
            val items =
                repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileId)
                    .whereEqualTo("gigerId", repository.getUID()).get()
                    .await()

            if (items.documents.isNullOrEmpty()) {
                return JpApplication(JPId = jobProfileId, gigerId = repository.getUID())
            }
            val toObject = items.toObjects(JpApplication::class.java).get(0)
            toObject.id = items.documents[0].id
            return toObject
        } catch (e: Exception) {
            _observableError.value = e.message;
        }
        return JpApplication()


    }

    suspend fun checkForQuestionnaire(jobProfileId: String, type: String, title: String): Boolean {
        try {
            val items =
                repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileId)
                    .whereEqualTo("gigerId", repository.getUID()).get()
                    .await()
            if (items.documents.isNullOrEmpty()) {
                return false
            }
            val documentSnapshot = items.documents[0]
            return !repository.db.collection("JP_Applications").document(documentSnapshot.id)
                .collection("Submissions").whereEqualTo("stepId", jobProfileId).whereEqualTo(
                    "title", title
                ).whereEqualTo("type", type).get().await().documents.isNullOrEmpty()
        } catch (e: Exception) {
            observableError.value = e.message
            return false
        }


    }


    suspend fun getProfile(): ProfileData? {
        return try {
            repository.db.collection("Profiles").document(repository.getUID()).get().await()
                .toObject(ProfileData::class.java)!!
        } catch (e: Exception) {
            observableError.value = e.message
            null
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
            var allCourseProgress =
                data.toObjects(GigActivationViewModel.CourseProgress::class.java).get(0)
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

    }


    var userProfileData: SingleLiveEvent<ProfileData> = SingleLiveEvent<ProfileData>()


}
