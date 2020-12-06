package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.client_activation.models.*
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ScheduleDrivingTestViewModel : ViewModel() {
    val repository = ScheduleDrivingTestRepository()

    private val _observableJPSettings = MutableLiveData<DocReceiving>()
    val observableJPSettings: MutableLiveData<DocReceiving> = _observableJPSettings

    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }

    val liveState: MutableLiveData<Lce<Int>> = MutableLiveData<Lce<Int>>()

    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableJpApplication: SingleLiveEvent<DrivingCertificate> by lazy {
        SingleLiveEvent<DrivingCertificate>();
    }
    val observableJpApplication: SingleLiveEvent<DrivingCertificate> get() = _observableJpApplication

    private val _observableApplied: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableApplied: SingleLiveEvent<Boolean> get() = _observableApplied
    fun getApplication(mWorkOrderID: String, type: String, title: String) = viewModelScope.launch {
        val model = getJPApplication(mWorkOrderID, type, title)
        _observableJpApplication.value = model

    }

    var verificationId: String? = null
    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            liveState.postValue(Lce.content(VERIFY_SUCCESS))
//            signInWithPhoneAuthCredentialScheduleDrivingTest(credential)
        }

        override fun onVerificationFailed(e: FirebaseException) {
            liveState.postValue(Lce.content(VERIFY_FAILED))
        }

        override fun onCodeSent(
                _verificationId: String,
                _token: PhoneAuthProvider.ForceResendingToken
        ) {
            super.onCodeSent(_verificationId, _token)
            verificationId = _verificationId
            liveState.postValue(Lce.content(CODE_SENT))
        }
    }

    suspend fun getJPApplication(
            workOrderID: String,
            type: String,
            title: String
    ): DrivingCertificate? {
        val items = repository.db.collection("JP_Applications").whereEqualTo("jpid", workOrderID)
                .whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await()
        if (submissions.documents.isNullOrEmpty()) {
            return null
        }

        return submissions.toObjects(DrivingCertSubmission::class.java)[0].certificate
    }

    fun getUIData(workOrderId: String) {

        repository.db.collection("JP_Settings").whereEqualTo("jobProfileId", workOrderId)
                .whereEqualTo("type", "doc_receiving").addSnapshotListener { success, err ->
                    if (err == null) {
                        if (success?.documents?.isNotEmpty() == true) {
                            _observableJPSettings.value =
                                    success?.toObjects(DocReceiving::class.java)?.get(0)

                        }
                    } else {
                        _observableError.value = err.message
                    }
                }

    }

    fun apply(
            mWorkOrderID: String, type: String, title: String, options: List<CheckItem>
    ) = viewModelScope.launch {


        setInJPApplication(
                mWorkOrderID,
                type,
                title,
                options
        )

    }


    suspend fun setInJPApplication(
            workOrderID: String,
            type: String,
            title: String, options: List<CheckItem>
    ) {
        val items = repository.getCollectionReference().whereEqualTo("jpid", workOrderID)
                .whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("submissions").whereEqualTo("stepId", workOrderID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await()

        repository.db.collection("JP_Applications")
                .document(items?.documents!![0].id)
                .collection("submissions")
                .document(submissions?.documents?.get(0)?.id!!)
                .update(
                        "certificate.options", options
                )
                .addOnCompleteListener { complete ->
                    if (complete.isSuccessful) {
                        val jpApplication =
                                items.toObjects(JpApplication::class.java)[0]
                        jpApplication.process.forEach { draft ->
                            if (draft.type == "onsite_document") {

                                options.forEach { item ->
                                    run {
                                        if (item.isForKitCollection) {
                                            draft.isDone = true
                                            draft.status="Done"
                                        }

                                    }
                                }
                            }

                            if (draft.title == title) {
                                draft.isDone = true
                                draft.isSlotBooked = true
                                draft.status = "Done"


                            }
                        }
                        if (jpApplication.process.all {
                                    it.isDone
                                }) {
                            jpApplication.status = "Applied"
                        }
                        repository.db.collection("JP_Applications")
                                .document(items.documents[0].id)
                                .update(mapOf("process" to jpApplication.process,
                                        "status" to jpApplication.status
                                ))
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        observableApplied.value = true
                                    }
                                }
                    }
                }


    }


    fun verifyPhoneNumberWithCodeScheduleDrivingTest(code: String) {
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        signInWithPhoneAuthCredentialScheduleDrivingTest(credential)
    }

    private fun signInWithPhoneAuthCredentialScheduleDrivingTest(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        liveState.postValue(Lce.content(VERIFY_SUCCESS))

                    } else {
                        liveState.postValue(Lce.content(VERIFY_FAILED))
                    }
                }
    }

    companion object {
        val CODE_SENT = 2;
        val VERIFY_FAILED = 3;
        val VERIFY_SUCCESS = 4;
    }


}