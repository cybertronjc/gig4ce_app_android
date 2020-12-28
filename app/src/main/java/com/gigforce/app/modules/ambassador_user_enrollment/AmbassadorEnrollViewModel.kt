package com.gigforce.app.modules.ambassador_user_enrollment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorApplication
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorEnrollmentProfile
import com.gigforce.app.modules.ambassador_user_enrollment.models.AmbassadorProfiles
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.gigforce.app.utils.SingleLiveEvent
import com.gigforce.app.utils.lang_models.LangMapSingleton
import com.google.firebase.firestore.ListenerRegistration
import java.util.*

class AmbassadorEnrollViewModel constructor(
    private val ambassadorEnrollmentRepository: AmbassadorEnrollmentRepository = AmbassadorEnrollmentRepository()
) : ViewModel() {

    private val _enrolledUsers = MutableLiveData<List<EnrolledUser>>()
    val enrolledUsers: LiveData<List<EnrolledUser>> = _enrolledUsers

    private var enrolledUserListener: ListenerRegistration? = null

    private val _observableEnrollmentProfile: SingleLiveEvent<AmbassadorEnrollmentProfile> by lazy {
        SingleLiveEvent<AmbassadorEnrollmentProfile>();
    }
    val observableEnrollmentProfile: SingleLiveEvent<AmbassadorEnrollmentProfile> get() = _observableEnrollmentProfile

    init {
        startWatchingEnrolledUsersList()
    }

    private fun startWatchingEnrolledUsersList() {
        enrolledUserListener = ambassadorEnrollmentRepository
            .getEnrolledUsersQuery()
            .addSnapshotListener { value, error ->
                error?.printStackTrace()

                value?.let {
                    val enrolledUsers = it.documents.map {
                        it.toObject(EnrolledUser::class.java)!!.apply {
                            this.id = it.id
                        }
                    }

                    _enrolledUsers.postValue(enrolledUsers)
                }
            }
    }

    override fun onCleared() {
        super.onCleared()
        enrolledUserListener?.remove()
    }
    fun getAmbassadorApplication(ambProgID: String) {

        ambassadorEnrollmentRepository.db.collection("Ambassador_Settings")
            .whereEqualTo("type", "enrollment_profile")
            .whereEqualTo("ambassadorProgramId", ambProgID)
            .addSnapshotListener { success, err ->
                if (!success?.documents.isNullOrEmpty()) {
                    run {
                        if (Locale.getDefault().displayName != "en") {
                            ambassadorEnrollmentRepository.db.collection("Configuration")
                                .document("AmbassadorEnrolmentTranslations")
                                .addSnapshotListener { success_, err_ ->
                                    run {
                                        LangMapSingleton.langMap =
                                            success_?.data?.get(Locale.getDefault().language) as? MutableMap<String, Any>?
                                    }
                                    val toObject =
                                        success?.documents!![0]?.toObject(AmbassadorEnrollmentProfile::class.java)
                                    toObject?.checkForLangTranslation()
                                    _observableEnrollmentProfile.value = toObject
                                }
                        }
                    }
                }

            }
    }
}