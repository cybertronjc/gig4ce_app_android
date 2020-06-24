package com.gigforce.app.modules.gigerVerfication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration

data class GigerVerificationStatus(
    val selfieVideoUploaded: Boolean,
    val panCardDetailsUploaded: Boolean,
    val aadharCardDetailsUploaded: Boolean,
    val dlCardDetailsUploaded: Boolean,
    val bankDetailsUploaded: Boolean,
    val everyDocumentUploaded: Boolean
)

class GigVerificationViewModel constructor(
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository()
) : ViewModel() {

    private val _gigerVerificationStatus = MutableLiveData<GigerVerificationStatus>()
    val gigerVerificationStatus: LiveData<GigerVerificationStatus> get() = _gigerVerificationStatus

    private var verificationChangesListener: ListenerRegistration? = null

    fun startListeningForGigerVerificationStatusChanges() {
        verificationChangesListener = gigerVerificationRepository
            .getDBCollection()
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                val docSnap = documentSnapshot ?: return@addSnapshotListener

                val selfieUploaded =
                    docSnap.contains(GigerVerificationDatabaseConstants.SELFIE_VIDEO_PARENT_NAME)

                val panDetailsUploaded =
                    docSnap.contains(GigerVerificationDatabaseConstants.PAN_CARD_DOC_PARENT_NAME)

                val dlDetailsUploaded =
                    docSnap.contains(GigerVerificationDatabaseConstants.DRIVING_LICENSE_PARENT_NAME)

                val aadharCardDetailsUploaded =
                    docSnap.contains(GigerVerificationDatabaseConstants.AADHAR_CARD_DOC_PARENT_NAME)

                val bankDetailsUploaded =
                    docSnap.contains(GigerVerificationDatabaseConstants.BANK_DETAILS_DOC_PARENT_NAME)

                val everyDocumentUploaded = selfieUploaded
                        && panDetailsUploaded
                        && dlDetailsUploaded
                        && aadharCardDetailsUploaded
                        && bankDetailsUploaded

                _gigerVerificationStatus.value = GigerVerificationStatus(
                    selfieVideoUploaded = selfieUploaded,
                    panCardDetailsUploaded = panDetailsUploaded,
                    aadharCardDetailsUploaded = aadharCardDetailsUploaded,
                    dlCardDetailsUploaded = dlDetailsUploaded,
                    bankDetailsUploaded = bankDetailsUploaded,
                    everyDocumentUploaded = everyDocumentUploaded
                )
            }
    }

    override fun onCleared() {
        super.onCleared()
        verificationChangesListener?.remove()
    }
}