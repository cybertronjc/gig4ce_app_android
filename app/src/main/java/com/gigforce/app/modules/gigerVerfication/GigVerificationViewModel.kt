package com.gigforce.app.modules.gigerVerfication

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.SelfieVideoDataModel
import com.gigforce.app.utils.Lse
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
)

open class GigVerificationViewModel constructor(
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository(),
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val gigerVerification3rdPartyStatusRepository: GigerVerification3rdPartyStatusRepository = GigerVerification3rdPartyStatusRepository()
) : ViewModel() {

    private val _gigerVerificationStatus = MutableLiveData<GigerVerificationStatus>()
    val gigerVerificationStatus: LiveData<GigerVerificationStatus> get() = _gigerVerificationStatus

    private val _documentUploadState = MutableLiveData<Lse>()
    val documentUploadState: LiveData<Lse> get() = _documentUploadState

    private var verificationChangesListener: ListenerRegistration? = null

    fun startListeningForGigerVerificationStatusChanges() {
        verificationChangesListener = gigerVerificationRepository
            .getDBCollection()
            .addSnapshotListener { documentSnapshot, firebaseFirestoreException ->

                if (documentSnapshot?.data == null)
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

                val docSnap = documentSnapshot ?: return@addSnapshotListener
                docSnap.toObject(VerificationBaseModel::class.java)?.let {

                    val everyDocumentUploaded = it.aadhar_card?.userHasAadharCard != null
                            && it.pan_card?.userHasPanCard != null
                            && it.bank_details?.userHasPassBook != null
                            && it.driving_license?.userHasDL != null
                            && it.selfie_video != null

                    _gigerVerificationStatus.value = GigerVerificationStatus(
                        selfieVideoUploaded = it.selfie_video != null,
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
                _gigerVerificationStatus.value = GigerVerificationStatus(
                    selfieVideoUploaded = it.selfie_video != null,
                    selfieVideoDataModel = it.selfie_video,
                    panCardDetailsUploaded = it.pan_card?.userHasPanCard != null,
                    panCardDetails = it.pan_card,
                    aadharCardDetailsUploaded = it.aadhar_card?.userHasAadharCard != null,
                    aadharCardDataModel = it.aadhar_card,
                    dlCardDetailsUploaded = it.driving_license?.userHasDL != null,
                    drivingLicenseDataModel = it.driving_license,
                    bankDetailsUploaded = it.bank_details?.userHasPassBook != null,
                    bankUploadDetailsDataModel = it.bank_details,
                    everyDocumentUploaded = false
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
            if (userHasPan)
                uploadPanInfoToThirdParty(
                    panImage!!,
                    panCardNo!!
                )
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to upload Document."))
            return@launch
        }

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
                panCardNo = panCardNo
            )
            gigerVerificationRepository.getDBCollection().setOrThrow(model)

            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    private suspend fun uploadPanInfoToThirdParty(
        panImage: Uri,
        panCardNo: String
    ) {

    }


    fun updateBankPassbookImagePath(
        userHasPassBook: Boolean,
        passbookImagePath: Uri?,
        ifscCode: String?,
        accountNo: String?
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {
            if (userHasPassBook)
                uploadBankInfoToThirdParty(
                    passbookImagePath,
                    ifscCode,
                    accountNo
                )
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to upload Document."))
            return@launch
        }

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
                accountNo = accountNo
            )
            gigerVerificationRepository.getDBCollection().setOrThrow(model)

            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }

    }

    private fun uploadBankInfoToThirdParty(
        passbookImagePath: Uri?,
        ifscCode: String?,
        accountNo: String?
    ) {
    }

    fun updateAadharData(
        userHasAadhar: Boolean,
        frontImagePath: Uri?,
        backImagePath: Uri?,
        aadharCardNumber: String?
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {
            if (userHasAadhar)
                uploadAadharInfoToThirdParty(
                    frontImagePath,
                    aadharCardNumber
                )
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to upload Document."))
            return@launch
        }


        try {
            val model = getVerificationModel()
            if (!userHasAadhar) {
                model.aadhar_card = AadharCardDataModel(
                    userHasAadharCard = false,
                    frontImage = null,
                    backImage = null,
                    verified = false,
                    aadharCardNo = null
                )
            } else {

                val frontImageFileNameAtServer = if (userHasAadhar)
                    uploadImage(frontImagePath!!)
                else
                    null

                val backImageFileNameAtServer = if (userHasAadhar)
                    uploadImage(backImagePath!!)
                else
                    null

                model.aadhar_card = AadharCardDataModel(
                    userHasAadharCard = true,
                    frontImage = frontImageFileNameAtServer,
                    backImage = backImageFileNameAtServer,
                    verified = false,
                    aadharCardNo = aadharCardNumber
                )

            }
            gigerVerificationRepository.getDBCollection().setOrThrow(model)
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    private fun uploadAadharInfoToThirdParty(frontImagePath: Uri?, aadharCardNumber: String?) {
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
            if (userHasDL)
                uploadDLInfoToThirdParty(
                    frontImagePath,
                    backImagePath,
                    dlState,
                    dlNo
                )
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to upload Document."))
            return@launch
        }


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
                    dlNo = dlNo
                )
            }
            gigerVerificationRepository.getDBCollection().setOrThrow(model)
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    private fun uploadDLInfoToThirdParty(
        frontImagePath: Uri?,
        backImagePath: Uri?,
        dlState: String?,
        dlNo: String?
    ) {
    }


    private fun prepareUniqueImageName(): String {
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        return gigerVerificationRepository.getUID() + timeStamp + ".jpg"
    }

    private suspend fun uploadImage(image: Uri) =
        suspendCoroutine<String> { continuation ->
            val fileNameAtServer = prepareUniqueImageName()
            firebaseStorage.reference
                .child("verification")
                .child(fileNameAtServer)
                .putFile(image)
                .addOnSuccessListener {
                    continuation.resume(fileNameAtServer)
                }
                .addOnFailureListener {
                    continuation.resumeWithException(it)
                }
        }

    private suspend fun getVerificationModel(): VerificationBaseModel =
        suspendCoroutine { continuation ->
            gigerVerificationRepository.getDBCollection().get().addOnSuccessListener {
                kotlin.runCatching {
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