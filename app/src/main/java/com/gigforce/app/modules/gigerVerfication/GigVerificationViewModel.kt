package com.gigforce.app.modules.gigerVerfication

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.R
import com.gigforce.core.di.repo.UserEnrollmentRepository
import com.gigforce.app.utils.Lse
import com.gigforce.core.SingleLiveEvent2
import com.gigforce.core.datamodels.verification.*
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.utils.EventLogs.setOrThrow
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class GigerVerificationStatus(
    val selfieVideoUploaded: Boolean = false,
    val selfieVideoDataModel: SelfieVideoDataModel? = null,
    val panCardDetailsUploaded: Boolean = false,
    val panCardDetails: PanCardDataModel? = null,
    val aadharCardDetailsUploaded: Boolean = false,
    val aadharCardDataModel: AadharCardDataModel? = null,
    val dlCardDetailsUploaded: Boolean = false,
    val drivingLicenseDataModel: DrivingLicenseDataModel? = null,
    val bankDetailsUploaded: Boolean = false,
    val bankUploadDetailsDataModel: BankDetailsDataModel? = null,
    val everyDocumentUploaded: Boolean = false


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
@HiltViewModel
open class GigVerificationViewModel @Inject constructor(
    private val buildConfig: IBuildConfigVM
) : ViewModel() {
    private val gigerVerificationRepository: GigerVerificationRepository = GigerVerificationRepository()
    private val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    private val userEnrollmentRepository: UserEnrollmentRepository = UserEnrollmentRepository(buildConfig = buildConfig)

    var redirectToNextStep: Boolean = false

    private val _gigerVerificationStatus = MutableLiveData<GigerVerificationStatus>()
    val gigerVerificationStatus: LiveData<GigerVerificationStatus> get() = _gigerVerificationStatus
    private val _gigerContractStatus = MutableLiveData<String>()
    val gigerContractStatus: LiveData<String> get() = _gigerContractStatus

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
                        bankDetailsUploaded = it.bank_details?.userHasPassBook != null && it.bank_details?.userHasPassBook == true,
                        bankUploadDetailsDataModel = it.bank_details,
                        everyDocumentUploaded = everyDocumentUploaded
                    )
                }
            }
    }

    fun getVerificationStatus(
        userId: String? = null
    ) = viewModelScope.launch {

        try {
            getVerificationModel(userId).let {

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
                    bankDetailsUploaded = it.bank_details?.userHasPassBook != null && it.bank_details?.userHasPassBook == true,
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

            val model = getVerificationModel()

            val fileNameAtServer = if (userHasPan && panImage != null)
                uploadImage(panImage)
            else
                model.pan_card?.panCardImagePath

            model.pan_card =
                PanCardDataModel(
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

    fun updatePanImagePath(
        userHasPan: Boolean,
        panImage: Uri?,
        panCardNo: String?,
        userId: String
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {

            val model = getVerificationModel(userId)

            val fileNameAtServer = if (userHasPan && panImage != null)
                uploadImage(panImage)
            else
                model.pan_card?.panCardImagePath

            model.pan_card =
                PanCardDataModel(
                    userHasPanCard = userHasPan,
                    panCardImagePath = fileNameAtServer,
                    verified = false,
                    panCardNo = panCardNo,
                    state = -1,
                    verifiedString = "Under Verification"
                )
            model.sync_status = false
            gigerVerificationRepository.getCollectionReference().document(userId).setOrThrow(model)

            if (userHasPan)
                userEnrollmentRepository.setPANDetailsAsUploaded(userId)

//            val fileNameAtServer = if (userHasPan && panImage != null)
//                uploadImage(panImage)
//            else {
//                val model = getVerificationModel()
//                model.pan_card?.panCardImagePath
//            }

//            gigerVerificationRepository.updatePanInfo(
//                userId = userId,
//                userHasPanCard = userHasPan,
//                fileNameAtServer = fileNameAtServer,
//                panCardNo = panCardNo
//            )
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
            val model = getVerificationModel()

            val fileNameAtServer = if (userHasPassBook && passbookImagePath != null)
                uploadImage(passbookImagePath)
            else
                model.bank_details?.passbookImagePath

            model.bank_details =
                BankDetailsDataModel(
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

    fun updateBankPassbookImagePath(
        userHasPassBook: Boolean,
        passbookImagePath: Uri?,
        ifscCode: String?,
        bankName: String?,
        accountNo: String?,
        userId: String
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {

            val model = getVerificationModel(userId)

            val fileNameAtServer = if (userHasPassBook && passbookImagePath != null)
                uploadImage(passbookImagePath)
            else
                model.bank_details?.passbookImagePath

            model.bank_details =
                BankDetailsDataModel(
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

            gigerVerificationRepository.getCollectionReference().document(userId).setOrThrow(model)

            if (userHasPassBook)
                userEnrollmentRepository.setBankDetailsAsUploaded(userId)


//            val fileNameAtServer = if (userHasPassBook && passbookImagePath != null)
//                uploadImage(passbookImagePath)
//            else {
//                val model = getVerificationModel()
//                model.bank_details?.passbookImagePath
//            }
//
//            gigerVerificationRepository.updateBankDetails(
//                userId = userId,
//                userHasPassBook = userHasPassBook,
//                passbookImagePath = fileNameAtServer,
//                ifscCode = ifscCode,
//                bankName = bankName,
//                accountNo = accountNo
//            )

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
                model.aadhar_card =
                    AadharCardDataModel(
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

                model.aadhar_card =
                    AadharCardDataModel(
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

    fun updateDLDataClientActivation(
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
                model.driving_license =
                    DrivingLicenseDataModel(
                        userHasDL = false,
                        verified = false,
                        frontImage = null,
                        backImage = null,
                        dlState = null,
                        dlNo = null
                    )
            } else {

                val frontImageFileNameAtServer = if (userHasDL && frontImagePath != null)
                    uploadImage(frontImagePath)
                else
                    model.driving_license?.frontImage


                val backImageFileNameAtServer = if (userHasDL && backImagePath != null)
                    uploadImage(backImagePath)
                else
                    model.driving_license?.backImage

                model.driving_license =
                    DrivingLicenseDataModel(
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

    fun updateAadharData(
        userHasAadhar: Boolean,
        frontImagePath: Uri?,
        backImagePath: Uri?,
        aadharCardNumber: String?,
        userId: String
    ) = viewModelScope.launch {
        _documentUploadState.postValue(Lse.loading())

        try {

            val model = getVerificationModel(userId)
            if (!userHasAadhar) {
                model.aadhar_card =
                    AadharCardDataModel(
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

                model.aadhar_card =
                    AadharCardDataModel(
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
            gigerVerificationRepository.getCollectionReference().document(userId).setOrThrow(model)

            if (userHasAadhar)
                userEnrollmentRepository.setAadharAsUploaded(userId)

//            if (!userHasAadhar) {
//                gigerVerificationRepository.updateAadharInfo(
//                    userId = userId,
//                    userHasAadharCard = userHasAadhar,
//                    frontImagePathAtServer = null,
//                    backImagePathAtServer = null,
//                    aadharNo = aadharCardNumber
//                )
//            } else {
//                val model = getVerificationModel()
//                val frontImageFileNameAtServer = if (userHasAadhar && frontImagePath != null)
//                    uploadImage(frontImagePath)
//                else
//                    model.aadhar_card?.frontImage
//
//                val backImageFileNameAtServer = if (userHasAadhar && backImagePath != null)
//                    uploadImage(backImagePath)
//                else
//                    model.aadhar_card?.backImage
//
//                gigerVerificationRepository.updateAadharInfo(
//                    userId = userId,
//                    userHasAadharCard = userHasAadhar,
//                    frontImagePathAtServer = frontImageFileNameAtServer,
//                    backImagePathAtServer = backImageFileNameAtServer,
//                    aadharNo = aadharCardNumber
//                )
//            }
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
                model.driving_license =
                    DrivingLicenseDataModel(
                        userHasDL = false,
                        verified = false,
                        frontImage = null,
                        backImage = null,
                        dlState = null,
                        dlNo = null
                    )
            } else {

                val frontImageFileNameAtServer = if (userHasDL && frontImagePath != null)
                    uploadImage(frontImagePath)
                else
                    model.driving_license?.frontImage


                val backImageFileNameAtServer = if (userHasDL && backImagePath != null)
                    uploadImage(backImagePath)
                else
                    model.driving_license?.backImage

                model.driving_license =
                    DrivingLicenseDataModel(
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

    fun updateDLData(
        userHasDL: Boolean,
        frontImagePath: Uri?,
        backImagePath: Uri?,
        dlState: String?,
        dlNo: String?,
        userId: String
    ) = viewModelScope.launch {

        _documentUploadState.postValue(Lse.loading())

        try {

            val model = getVerificationModel(userId)
            if (!userHasDL) {
                model.driving_license =
                    DrivingLicenseDataModel(
                        userHasDL = false,
                        verified = false,
                        frontImage = null,
                        backImage = null,
                        dlState = null,
                        dlNo = null
                    )
            } else {

                val frontImageFileNameAtServer = if (userHasDL && frontImagePath != null)
                    uploadImage(frontImagePath)
                else
                    model.driving_license?.frontImage

                val backImageFileNameAtServer = if (userHasDL && backImagePath != null)
                    uploadImage(backImagePath)
                else
                    model.driving_license?.backImage

                model.driving_license =
                    DrivingLicenseDataModel(
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
            gigerVerificationRepository.getCollectionReference().document(userId).setOrThrow(model)

            if (userHasDL)
                userEnrollmentRepository.setDrivingDetailsAsUploaded(userId)

//            if (!userHasDL) {
//                gigerVerificationRepository.updateDlDetails(
//                    userId = userId,
//                    userHasDL = userHasDL,
//                    frontImageFileNameAtServer = null,
//                    backImageFileNameAtServer = null,
//                    dlState = null,
//                    dlNo = null
//                )
//            } else {
//                val model = getVerificationModel()
//
//                val frontImageFileNameAtServer = if (userHasDL && frontImagePath != null)
//                    uploadImage(frontImagePath)
//                else
//                    model.driving_license?.frontImage
//
//                val backImageFileNameAtServer = if (userHasDL && backImagePath != null)
//                    uploadImage(backImagePath)
//                else
//                    model.driving_license?.backImage
//
//                gigerVerificationRepository.updateDlDetails(
//                    userId = userId,
//                    userHasDL = userHasDL,
//                    frontImageFileNameAtServer = frontImageFileNameAtServer,
//                    backImageFileNameAtServer = backImageFileNameAtServer,
//                    dlState = dlState,
//                    dlNo = dlNo
//                )
//            }
            _documentUploadState.postValue(Lse.success())
        } catch (e: Exception) {
            _documentUploadState.postValue(Lse.error("Unable to save document."))
        }
    }

    fun uploadDLCer(
        userHasDL: Boolean,
        frontImagePath: Uri?
    ) = viewModelScope.launch {

//        _documentUploadState.postValue(Lse.loading())
//
//        try {
//            val model = getVerificationModel()
//
//            val frontImageFileNameAtServer =
//                    uploadImage(frontImagePath!!)
//
//            model.drivingCert = DrivingCertificate(
//                    verified = false,
//                    frontImage = frontImageFileNameAtServer
//
//            )
//            gigerVerificationRepository.getDBCollection().setOrThrow(model)
//            _documentUploadState.postValue(Lse.success())
//        } catch (e: Exception) {
//            _documentUploadState.postValue(Lse.error("Unable to save document."))
//        }
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

    suspend fun getVerificationModel(userId: String? = null): VerificationBaseModel =
        suspendCoroutine { continuation ->

            val docRef = if (userId != null) {
                gigerVerificationRepository.getCollectionReference().document(userId)
            } else {
                gigerVerificationRepository.getDBCollection()
            }
            docRef.get().addOnSuccessListener {
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

    fun checkForSignedContract() {
        gigerVerificationRepository.checkForSignedContract().addSnapshotListener { success, err ->
            run {
                if (err == null) {
                    val verification = success?.toObject(VerificationBaseModel::class.java)

                    if (verification?.contract?.role != null && verification.contract?.url != null) {
                        _gigerContractStatus.value = verification.contract?.url
                    } else {
                        _gigerContractStatus.value = null
                    }

                } else {
                    _gigerContractStatus.value = null

                }


            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        verificationChangesListener?.remove()
    }
}