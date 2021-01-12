package com.gigforce.app.modules.client_activation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.ambassador_user_enrollment.models.RegisterMobileNoResponse
import com.gigforce.app.modules.ambassador_user_enrollment.models.VerifyOtpResponse
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.UserEnrollmentRepository
import com.gigforce.app.modules.auth.ui.main.LoginResponse
import com.gigforce.app.modules.client_activation.models.*
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.SingleLiveEvent
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ScheduleDrivingTestViewModel : ViewModel() {
    var applicationId = ""
    var submissionId = ""
    val repository = ScheduleDrivingTestRepository()
    val userEnrollmentRepository = UserEnrollmentRepository()

    private val _observableJPSettings = MutableLiveData<DocReceiving>()
    val observableJPSettings: MutableLiveData<DocReceiving> = _observableJPSettings

    private val _sendOTP = MutableLiveData<Lce<RegisterMobileNoResponse>>()
    val sendOTP: MutableLiveData<Lce<RegisterMobileNoResponse>> = _sendOTP
    private val _verifyOTP = MutableLiveData<Lce<VerifyOtpResponse>>()
    val verifyOTP: MutableLiveData<Lce<VerifyOtpResponse>> = _verifyOTP
    private val _observableError: SingleLiveEvent<String> by lazy {
        SingleLiveEvent<String>();
    }

    val liveState: MutableLiveData<LoginResponse> = MutableLiveData<LoginResponse>()

    val observableError: SingleLiveEvent<String> get() = _observableError

    private val _observableJpApplication: SingleLiveEvent<DrivingCertificate> by lazy {
        SingleLiveEvent<DrivingCertificate>();
    }
    val observableJpApplication: SingleLiveEvent<DrivingCertificate> get() = _observableJpApplication

    private val _observableApplied: SingleLiveEvent<Boolean> by lazy {
        SingleLiveEvent<Boolean>();
    }
    val observableApplied: SingleLiveEvent<Boolean> get() = _observableApplied
    var otpVerificationToken: String = ""
    fun getApplication(mJobProfileId: String, type: String, title: String) = viewModelScope.launch {
        val model = getJPApplication(mJobProfileId, type, title)
        if (model != null) {
            _observableJpApplication.value = model

        }
    }

    suspend fun getJPApplication(
            jobProfileID: String,
            type: String,
            title: String
    ): DrivingCertificate? {
        try {
            val items =
                    repository.db.collection("JP_Applications").whereEqualTo("jpid", jobProfileID)
                            .whereEqualTo("gigerId", repository.getUID()).get()
                            .await()
            if (items.documents.isNullOrEmpty()) {
                return null
            }
            applicationId = items.documents[0].id
            val submissions = repository.getCollectionReference().document(items.documents[0].id)
                    .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                            "title", title
                    ).whereEqualTo("type", type).get().await()
            if (submissions.documents.isNullOrEmpty()) {
                return null
            }
            submissionId = submissions.documents[0].id

            return submissions.toObjects(CheckoutGigforceOffice::class.java)[0].certificate
        } catch (e: Exception) {
            _observableError.value = e.message
            return null
        }

    }

    fun getUIData(jobProfileID: String) {

        repository.db.collection("JP_Settings").limit(1).whereEqualTo("jobProfileId", jobProfileID)
                .whereEqualTo("type", "driving_certificate").addSnapshotListener { success, err ->
                    if (err == null) {
                        if (success?.documents?.isNotEmpty() == true) {
                            _observableJPSettings.value =
                                    success.toObjects(PartnerSchool::class.java)[0].checkoutConfig

                        }
                    } else {
                        _observableError.value = err.message
                    }
                }

    }

    fun apply(
            mJobProfileId: String, type: String, title: String, options: List<CheckItem>
    ) = viewModelScope.launch {


        setInJPApplication(
                mJobProfileId,
                type,
                title,
                options
        )

    }


    suspend fun setInJPApplication(
            jobProfileID: String,
            type: String,
            title: String, options: List<CheckItem>
    ) {
        val items = repository.getCollectionReference().whereEqualTo("jpid", jobProfileID)
                .whereEqualTo("gigerId", repository.getUID()).get()
                .await()
        val submissions = repository.getCollectionReference().document(items.documents[0].id)
                .collection("Submissions").whereEqualTo("stepId", jobProfileID).whereEqualTo(
                        "title", title
                ).whereEqualTo("type", type).get().await()

        val collection = repository.db.collection("JP_Applications")
                .document(items?.documents!![0].id)
                .collection("Submissions")

        val task = if (submissions?.documents?.isEmpty() == true)
            collection.document().set(mapOf("options" to options, "type" to type, "title" to title))
        else
            collection.document(submissions.documents[0].id).update("options", options)
        task.addOnCompleteListener { complete ->
            if (complete.isSuccessful) {
                val jpApplication =
                        items.toObjects(JpApplication::class.java)[0]
                jpApplication.activation.forEach { draft ->
                    if (draft.type == "onsite_document") {

                        options.forEach { item ->
                            run {
                                if (item.isForKitCollection) {
                                    draft.isDone = true
                                    draft.status = ""
                                }
                            }
                        }
                    }

                    if (draft.title == title) {
                        draft.isDone = true
                        draft.isSlotBooked = true
                        draft.status = ""


                    }
                }
                if (jpApplication.activation.all {
                            it.isDone
                        }) {
                    jpApplication.status = "Applied"
                }
                repository.db.collection("JP_Applications")
                        .document(items.documents[0].id)
                        .update(
                                mapOf(
                                        "activation" to jpApplication.activation,
                                        "status" to jpApplication.status
                                )
                        )
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                _observableApplied.value = true
                            } else {
                                _observableError.value = it.exception?.message ?: ""
                            }
                        }
            }
        }


    }

    fun sendOTPToMobile(
            mobileNo: String
    ) = viewModelScope.launch {

        _sendOTP.postValue(Lce.loading())
        try {
            val repsonse = userEnrollmentRepository.checkMobileForExistingRegistrationElseSendOtp(mobileNo)
            _sendOTP.value = Lce.content(repsonse)
        } catch (e: Exception) {
            e.printStackTrace()
            _sendOTP.value = Lce.error(e.message ?: "Unable to check mobile number")
            _sendOTP.value = null
        }
    }

    fun verifyOTP(
            otp: String
    ) = viewModelScope.launch {
        try {
            val verifyOtpResponse = userEnrollmentRepository.verifyOtp(otpVerificationToken, otp)

            if (verifyOtpResponse.isVerified) {
                _verifyOTP.value = Lce.content(verifyOtpResponse)
            } else {
                _verifyOTP.value = Lce.error("Not Verified")
            }

        } catch (e: Exception) {
            _verifyOTP.value = Lce.error(e.message ?: "Not Verified")
        }
    }


    companion object {
        val CODE_SENT = 2;
        val VERIFY_FAILED = 3;
        val VERIFY_SUCCESS = 4;
    }


//    fun downloadCertificate(_id: String, drivingCertificateID: String) = liveData(Dispatchers.IO) {
//        emit(Resource.loading(data = null))
//        try {
//            emit(
//                Resource.success(
//                    data = repository.getDrivingCertificate(
//                        _id,
//                        drivingCertificateID
//                    )
//                )
//            )
//        } catch (exception: Exception) {
//            emit(Resource.error(data = null, message = exception.message ?: "Error Occurred!"))
//        }
//    }

//    fun verifyPhoneNumberWithCodeScheduleDrivingTest(code: String) {
//        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
//        signInWithPhoneAuthCredentialScheduleDrivingTest(credential)
//    }

//    private fun signInWithPhoneAuthCredentialScheduleDrivingTest(credential: PhoneAuthCredential) {
//        FirebaseAuth.getInstance()
//            .signInWithCredential(credential)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    liveState.postValue(LoginResponse(VERIFY_SUCCESS, "OTP Verified"))
//
//                } else {
//                    liveState.postValue(LoginResponse(VERIFY_FAILED, it.exception?.message ?: ""))
//                }
//            }
//    }
//      var verificationId: String? = null
//    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//            liveState.postValue(LoginResponse(VERIFY_SUCCESS, "OTP Verified"))
////            signInWithPhoneAuthCredentialScheduleDrivingTest(credential)
//        }
//
//        override fun onVerificationFailed(e: FirebaseException) {
//            liveState.postValue(LoginResponse(VERIFY_FAILED, e.message ?: ""))
//        }
//
//        override fun onCodeSent(
//            _verificationId: String,
//            _token: PhoneAuthProvider.ForceResendingToken
//        ) {
//            super.onCodeSent(_verificationId, _token)
//            verificationId = _verificationId
//            liveState.postValue(LoginResponse(CODE_SENT, "OTP Sent Successfully"));
//        }
//    }
}