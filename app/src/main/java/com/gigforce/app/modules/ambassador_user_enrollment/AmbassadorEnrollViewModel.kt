package com.gigforce.app.modules.ambassador_user_enrollment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.ambassador_user_enrollment.models.EnrolledUser
import com.google.firebase.firestore.ListenerRegistration

class AmbassadorEnrollViewModel constructor(
    private val ambassadorEnrollmentRepository: AmbassadorEnrollmentRepository
) : ViewModel() {

    private val _enrolledUsers = MutableLiveData<List<EnrolledUser>>()
    val enrolledUsers: LiveData<List<EnrolledUser>> = _enrolledUsers

    private var enrolledUserListener: ListenerRegistration? = null

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
}