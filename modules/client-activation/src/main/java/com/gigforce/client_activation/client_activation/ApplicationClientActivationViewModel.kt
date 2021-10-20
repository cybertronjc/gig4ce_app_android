package com.gigforce.client_activation.client_activation

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.models.JpSettings
import com.gigforce.core.SingleLiveEvent
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.firestore.QueryDocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class ApplicationClientActivationViewModel : ViewModel() {
    var itemClicked: Int = -1

    val repository = ApplicationClientActivationRepository()

    var redirectToNextStep: Boolean = false

    private val _observableJobProfile: SingleLiveEvent<JpSettings> by lazy {
        SingleLiveEvent<JpSettings>()
    }
    val observableJobProfile: SingleLiveEvent<JpSettings> get() = _observableJobProfile

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>()
    }
    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableApplicationStatus: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>()
    }
    val observableApplicationStatus: SingleLiveEvent<Boolean> get() = _observableApplicationStatus

    private val _observableInitApplication: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>()
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
            Log.d("model", model.toString())

            Log.d("list", dependency.toString())
            if (model.application.isNullOrEmpty()) {
                model.application = dependency.toMutableList()

            }

            model.application.forEach {
                if (!it.isDone || it.refresh) {
                    Log.d("type", it.toString())
                    when (it.type) {
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

            var allStatus = arrayListOf("started", "completed", "failed")
            // need to change for KYC, profile pic and about_me
            val profileModel = getProfile()
            val verification = getVerification()
            model.application.forEach {
                when (it.type) {
                    "profile_pic" -> {
                        it.isDone =
                            !profileModel?.profileAvatarName.isNullOrEmpty() && profileModel?.profileAvatarName != "avatar.jpg"
                    }
                    "about_me" -> {
                        it.isDone = !profileModel?.aboutMe.isNullOrEmpty()
                    }
                    "driving_licence" -> {
                        it.isDone =
                            verification?.driving_license != null && (verification.driving_license?.status != null && allStatus.contains(
                                verification.driving_license?.status ?: ""
                            ) || verification.driving_license?.verified ?: false)
                    }
                    "aadhar_card" -> {
                        it.isDone =
                            verification?.aadhar_card != null && (verification.aadhar_card?.verified
                                ?: false)
                    }
                    "pan_card" -> {
                        it.isDone =
                            verification?.pan_card != null && (verification.pan_card?.status?.equals(
                                "started"
                            ) ?: false || verification.pan_card?.verified ?: false)
                    }
                    "bank_account" -> {
                        it.isDone =
                            verification?.bank_details != null && (verification.bank_details?.status?.equals(
                                "started"
                            ) ?: false || verification.bank_details?.verified ?: false)
                    }
                    "aadhar_card_questionnaire" -> {
                        it.isDone =
                            verification?.aadhaar_card_questionnaire != null && verification.aadhaar_card_questionnaire?.verified == true
                    }

                    "pf_esic" -> {
                        it.isDone =
                            (profileModel?.pfesic?.dobNominee?.isNotBlank() == true && profileModel.pfesic?.rNomineeName?.isNotBlank() == true && profileModel.pfesic?.nomineeName?.isNotBlank() == true && profileModel.pfesic?.signature?.isNotBlank() == true) || (profileModel?.pfesic?.uanNumber?.isNotBlank() == true && profileModel.pfesic?.pfNumber?.isNotBlank() == true && profileModel.pfesic?.esicNumber?.isNotBlank() == true)
                    }
                }
            }


            if (model.id.isEmpty()) {
                repository.db.collection("JP_Applications").document().set(model)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            draftApplication(mJobProfileId)
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


    fun apply1(jobProfileId: String) {
        repository.db.collection("JP_Settings").whereEqualTo("jobProfileId", jobProfileId)
            .whereEqualTo("type", "activation").get()
            .addOnCompleteListener(OnCompleteListener<QuerySnapshot> {
                if (it.isSuccessful) {
                    for (document: QueryDocumentSnapshot in it.result!!) {
                        isActivationScreenFound = true
                    }

                } else {
                    Log.d("CITITES_REPO", "Error getting documents: ", it.exception)
                }

                apply(jobProfileId)
            })
    }

    fun apply(jobProfileId: String) = viewModelScope.launch {
        val application = getJPApplication(jobProfileId)
        var statusUpdate = mapOf(
            "stepsTotal" to (observableJobProfile.value?.step ?: 0),
            "status" to "Submitted",
            "applicationComplete" to Date()
        )
        if (isActivationScreenFound) {
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
                    observableError.value = it.exception?.message ?: ""
                }
            }

    }

    //    private val _observableGigActivation = MutableLiveData<Boolean>()
//    val observableGigActivation: LiveData<Boolean> = _observableGigActivation
    var isActivationScreenFound = false

    fun draftApplication(jobProfileId: String) = viewModelScope.launch {
        val application = getJPApplication(jobProfileId)
        if (application.status == "" && application.id != "") {
            repository.db.collection("JP_Applications").document(application.id)
                .update(
                    mapOf(
                        "status" to "Interested"
                    )
                )
                .addOnCompleteListener {
                    if (it.isSuccessful) {
//                        observableApplicationStatus.value = true
                        //need to comment : navigating to Application submitted screen
                    } else {
                        observableError.value = it.exception?.message ?: ""
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
            _observableError.value = e.message
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


    var userProfileData: SingleLiveEvent<ProfileData> =
        SingleLiveEvent<ProfileData>()


}
