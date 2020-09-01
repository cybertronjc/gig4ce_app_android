package com.gigforce.app.modules.gigerVerfication

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.R
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.SelfieVideoDataModel
import com.gigforce.app.utils.Lse
import com.gigforce.app.utils.SingleLiveEvent2
import com.gigforce.app.utils.setOrThrow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class GigerVerificationStatus(
    val selfieVideoUploaded: Boolean,
    val selfieVideoDataModel: SelfieVideoDataModel?,
    val panCardDetailsUploaded: Boolean,
    val panCardDetails: PanCardDataModel?,
    val aadharCardDetailsUploaded: Boolean,
    val aadharCardDataModel: AadharCardDataModel?,
    val dlCardDetailsUploaded: Boolean,
    val drivingLicenseDataModel: DrivingLicenseDataModel?,
    val bankDetailsUploaded: Boolean,
    val bankUploadDetailsDataModel: BankDetailsDataModel?,
    val everyDocumentUploaded: Boolean
) {
    fun getColorCodeForStatus(statusCode: Int): Int {
        return when (statusCode) {
            STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY, STATUS_DOCUMENT_UPLOADED, STATUS_DOCUMENT_PROCESSING -> R.color.yellow
            STATUS_VERIFIED -> R.color.green
            STATUS_VERIFICATION_FAILED -> R.color.app_red
            else -> R.color.yellow
        }
    }

    companion object {
        const val STATUS_VERIFIED = 2
        const val STATUS_VERIFICATION_FAILED = 3
        const val STATUS_DOCUMENT_RECEIVED_BY_3RD_PARTY = 0
        const val STATUS_DOCUMENT_PROCESSING = 1
        const val STATUS_DOCUMENT_UPLOADED = -1
    }
}

open class GigVerificationViewModel constructor(
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
) : ViewModel() {

    private val _gigerVerificationStatus = MutableLiveData<GigerVerificationStatus>()
    val gigerVerificationStatus: LiveData<GigerVerificationStatus> get() = _gigerVerificationStatus

    private val _documentUploadState = SingleLiveEvent2<Lse>()
    val documentUploadState: LiveData<Lse> get() = _documentUploadState

    private var verificationChangesListener: ListenerRegistration? = null

    fun startListeningForGigerVerificationStatusChanges() {
        verificationChangesListener = gigerVerificationRepository
            .getDBCollection()
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.data == null && firebaseFirestoreException == null) {
                    _gigerVerificationStatus.value = GigerVerificationStatus(
                        selfieVideoUploaded = false,
                        selfieVideoDataModel = null,
                        panCardDetailsUploaded = false,
                        panCardDetails = null,
                        aadharCardDetailsUploaded = false,
                        aadharCardDataModel = null,
                        dlCardDetailsUploaded = false,
                        drivingLicenseDataModel = null,
                        bankDetailsUploaded = false,
                        bankUploadDetailsDataModel = null,
                        everyDocumentUploaded = false
                    )
                }

                val docSnap = documentSnapshot ?: return@addSnapshotListener
                docSnap.toObject(VerificationBaseModel::class.java)?.let {

                    val everyDocumentUploaded = it.aadhar_card?.userHasAadharCard != null
                            && it.pan_card?.userHasPanCard != null
                            && it.bank_details?.userHasPassBook != null
                            && it.driving_license?.userHasDL != null
                            && it.selfie_video?.videoPath != null

                    _gigerVerificationStatus.value = GigerVerificationStatus(
                        selfieVideoUploaded = it.selfie_video?.videoPath != null,
                        selfieVideoDataModel = it.selfie_video,
                        panCardDetailsUploaded = it.pan_card?.userHasPanCard != null,
                        panCardDetails = it.pan_card,
                        aadharCardDetailsUploaded = it.aadhar_card?.userHasAadharCard != null,
                        aadharCardDataModel = it.aadhar_card,
                        dlCardDetailsUploaded = it.driving_license?.userHasDL != null,
                        drivingLicenseDataModel = it.driving_license,
                        bankDetailsUploaded = it.bank_details?.userHasPassBook != null,
                        bankUploadDetailsDataModel = it.bank_details,
                        everyDocumentUploaded = everyDocumentUploaded
                    )
                }
            }
    }

    fun getVerificationStatus() = viewModelScope.launch {

        try {
            getVerificationModel().let {

                val everyDocumentUploaded = it.aadhar_card?.userHasAadharCard != null
                        && it.pan_card?.userHasPanCard != null
                        && it.bank_details?.userHasPassBook != null
                        && it.driving_license?.userHasDL != null
                        && it.selfie_video?.videoPath != null

                _gigerVerificationStatus.value = GigerVerificationStatus(
                    selfieVideoUploaded = it.selfie_video?.videoPath != null,
                    selfieVideoDataModel = it.selfie_video,
                    panCardDetailsUploaded = it.pan_card?.userHasPanCard != null,
                    panCardDetails = it.pan_card,
                    aadharCardDetailsUploaded = it.aadhar_card?.userHasAadharCard != null,
                    aadharCardDataModel = it.aadhar_card,
                    dlCardDetailsUploaded = it.driving_license?.userHasDL != null,
                    drivingLicenseDataModel = it.driving_license,
                    bankDetailsUploaded = it.bank_details?.userHasPassBook != null,
                    bankUploadDetailsDataModel = it.bank_details,
                    everyDocumentUploaded = everyDocumentUploaded
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun updatePanImagePath(
        userHasPan: Boolean,
        panImage: Uri?,
        panCardNo: String?
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {
            val fileNameAtServer = if (userHasPan)
                uploadImage(panImage!!)
            else
                null

            val model = getVerificationModel()
            model.pan_card = PanCardDataModel(
                userHasPanCard = userHasPan,
                panCardImagePath = fileNameAtServer,
                verified = false,
                panCardNo = panCardNo,
                state = -1,
                verifiedString = "Under Verification"
            )
            model.sync_status = false

            gigerVerificationRepository.getDBCollection().setOrThrow(model)

            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    fun updateBankPassbookImagePath(
        userHasPassBook: Boolean,
        passbookImagePath: Uri?,
        ifscCode: String?,
        bankName: String?,
        accountNo: String?
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {
            val fileNameAtServer = if (userHasPassBook)
                uploadImage(passbookImagePath!!)
            else
                null

            val model = getVerificationModel()
            model.bank_details = BankDetailsDataModel(
                userHasPassBook = userHasPassBook,
                passbookImagePath = fileNameAtServer,
                verified = false,
                ifscCode = ifscCode,
                bankName = bankName,
                accountNo = accountNo,
                state = -1,
                verifiedString = "Under Verification"
            )
            model.sync_status = false

            gigerVerificationRepository.getDBCollection().setOrThrow(model)

            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }

    }

    fun updateAadharData(
        userHasAadhar: Boolean,
        frontImagePath: Uri?,
        backImagePath: Uri?,
        aadharCardNumber: String?
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {
            val model = getVerificationModel()
            if (!userHasAadhar) {
                model.aadhar_card = AadharCardDataModel(
                    userHasAadharCard = false,
                    frontImage = null,
                    backImage = null,
                    verified = false,
                    aadharCardNo = null,
                    state = -1,
                    verifiedString = "Under Verification"
                )
            } else {

                val frontImageFileNameAtServer = if (userHasAadhar && frontImagePath != null)
                    uploadImage(frontImagePath)
                else
                    model.aadhar_card?.frontImage

                val backImageFileNameAtServer = if (userHasAadhar && backImagePath != null)
                    uploadImage(backImagePath)
                else
                    model.aadhar_card?.backImage

                model.aadhar_card = AadharCardDataModel(
                    userHasAadharCard = true,
                    frontImage = frontImageFileNameAtServer,
                    backImage = backImageFileNameAtServer,
                    verified = false,
                    aadharCardNo = aadharCardNumber,
                    state = -1,
                    verifiedString = "Under Verification"
                )
                model.sync_status = false

            }
            gigerVerificationRepository.getDBCollection().setOrThrow(model)
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    fun updateDLData(
        userHasDL: Boolean,
        frontImagePath: Uri?,
        backImagePath: Uri?,
        dlState: String?,
        dlNo: String?
    ) = viewModelScope.launch {

        _documentUploadState.postValue(Lse.loading())

        try {
            val model = getVerificationModel()
            if (!userHasDL) {
                model.driving_license = DrivingLicenseDataModel(
                    userHasDL = false,
                    verified = false,
                    frontImage = null,
                    backImage = null,
                    dlState = null,
                    dlNo = null
                )
            } else {

                val frontImageFileNameAtServer = if (userHasDL)
                    uploadImage(frontImagePath!!)
                else
                    null

                val backImageFileNameAtServer = if (userHasDL)
                    uploadImage(backImagePath!!)
                else
                    null

                model.driving_license = DrivingLicenseDataModel(
                    userHasDL = true,
                    verified = false,
                    frontImage = frontImageFileNameAtServer,
                    backImage = backImageFileNameAtServer,
                    dlState = dlState,
                    dlNo = dlNo,
                    state = -1,
                    verifiedString = "Under Verification"
                )
                model.sync_status = false
            }
            gigerVerificationRepository.getDBCollection().setOrThrow(model)
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }


    private fun prepareUniqueImageName(): String {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return gigerVerificationRepository.getUID() + timeStamp + ".jpg"
    }

    private suspend fun uploadImage(image: Uri) =
        suspendCoroutine<String> { cont ->
            val fileNameAtServer = prepareUniqueImageName()
            val filePathOnServer = firebaseStorage.reference
                .child("verification")
                .child(fileNameAtServer)

            filePathOnServer
                .putFile(image)
                .addOnSuccessListener {
                    filePathOnServer
                        .downloadUrl
                        .addOnSuccessListener {
                            cont.resume(it.toString())

                        }.addOnFailureListener {
                            cont.resumeWithException(it)
                        }
                }
                .addOnFailureListener {
                    cont.resumeWithException(it)
                }
        }

    suspend fun getVerificationModel(): VerificationBaseModel =
        suspendCoroutine { continuation ->
            gigerVerificationRepository.getDBCollection().get().addOnSuccessListener {
                runCatching {
                    if (it.data == null)
                        VerificationBaseModel()
                    else
                        it.toObject(VerificationBaseModel::class.java)!!
                }.onSuccess {
                    continuation.resume(it)
                }.onFailure {
                    continuation.resumeWithException(it)
                }
            }.addOnFailureListener {
                continuation.resumeWithException(it)
            }
        }

    override fun onCleared() {
        super.onCleared()
        verificationChangesListener?.remove()
    }
}