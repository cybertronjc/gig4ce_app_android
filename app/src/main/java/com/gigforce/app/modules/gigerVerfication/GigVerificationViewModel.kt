package com.gigforce.app.modules.gigerVerfication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.SelfieVideoDataModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration

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

                val selfieUploaded = docSnap.contains(SelfieVideoDataModel.TABLE_NAME)
                val selfieVideoDetails = extractSelfieVideoInfo(selfieUploaded, docSnap)

                val panDetailsUploaded = docSnap.contains(PanCardDataModel.TABLE_NAME)
                val panCardDetails = extractPanCardInfo(panDetailsUploaded, docSnap)

                val dlDetailsUploaded =
                    docSnap.contains(DrivingLicenseDataModel.TABLE_NAME)
                val dlDetails = extractDlInfo(dlDetailsUploaded, docSnap)

                val aadharCardDetailsUploaded =
                    docSnap.contains(AadharCardDataModel.TABLE_NAME)
                val aadharInfo = extractAadharInfo(aadharCardDetailsUploaded, docSnap)

                val bankDetailsUploaded =
                    docSnap.contains(BankDetailsDataModel.TABLE_NAME)
                val bankDetails = extractBankDetailsInfo(bankDetailsUploaded, docSnap)

                val everyDocumentUploaded = selfieUploaded
                        && panDetailsUploaded
                        && dlDetailsUploaded
                        && aadharCardDetailsUploaded
                        && bankDetailsUploaded

                _gigerVerificationStatus.value = GigerVerificationStatus(
                    selfieVideoUploaded = selfieUploaded,
                    selfieVideoDataModel = selfieVideoDetails,
                    panCardDetailsUploaded = panDetailsUploaded,
                    panCardDetails = panCardDetails,
                    aadharCardDetailsUploaded = aadharCardDetailsUploaded,
                    aadharCardDataModel = aadharInfo,
                    dlCardDetailsUploaded = dlDetailsUploaded,
                    drivingLicenseDataModel = dlDetails,
                    bankDetailsUploaded = bankDetailsUploaded,
                    bankUploadDetailsDataModel = bankDetails,
                    everyDocumentUploaded = everyDocumentUploaded
                )
            }
    }

    private fun extractSelfieVideoInfo(
        selfieVideoUploaded: Boolean,
        docSnap: DocumentSnapshot
    ): SelfieVideoDataModel {
        var selfieVideoPath: String
        var videoVerified: Boolean = false


        try {

            if (selfieVideoUploaded) {
                val hashMap =
                    docSnap.get(SelfieVideoDataModel.TABLE_NAME) as HashMap<String, Any?>
                selfieVideoPath =
                    hashMap[SelfieVideoDataModel.KEY_NAME_VIDEO_PATH] as String
//                videoVerified =
//                    hashMap[SelfieVideoDataModel.KEY_NAME_VERIFIED] as Boolean

            } else {
                selfieVideoPath = ""
                videoVerified = false
            }
        } catch (e: Exception) {
            selfieVideoPath = ""
            videoVerified = false
        }

        return SelfieVideoDataModel(
            videoPath = selfieVideoPath,
            verified = videoVerified
        )
    }

    private fun extractPanCardInfo(
        panDetailsUploaded: Boolean,
        docSnap: DocumentSnapshot
    ): PanCardDataModel {
        var panCardImageName: String?
        var userHasPanCard: Boolean?
        var panVerified: Boolean

        try {

            if (panDetailsUploaded) {
                val hashMap =
                    docSnap.get(PanCardDataModel.TABLE_NAME) as HashMap<String, Any?>
                panCardImageName =
                    hashMap[PanCardDataModel.KEY_NAME_PASSBOOK_IMAGE_PATH] as String?
                userHasPanCard =
                    hashMap[PanCardDataModel.KEY_NAME_USER_HAS_PAN_CARD] as Boolean?
                panVerified =
                    hashMap[PanCardDataModel.KEY_NAME_VERIFIED] as Boolean
            } else {
                panCardImageName = null
                userHasPanCard = null
                panVerified = false
            }
        } catch (e: Exception) {
            panCardImageName = null
            userHasPanCard = null
            panVerified = false
        }

        val panCardDetails = PanCardDataModel(
            panCardImagePath = panCardImageName,
            userHasPanCard = userHasPanCard,
            verified = panVerified
        )
        return panCardDetails
    }


    private fun extractDlInfo(
        dlDetailsUploaded: Boolean,
        docSnap: DocumentSnapshot
    ): DrivingLicenseDataModel {
        var dlFrontImageName: String?
        var dlBackImageName: String?
        var userHasDL: Boolean?
        var dlVerified: Boolean

        try {

            if (dlDetailsUploaded) {
                val hashMap =
                    docSnap.get(DrivingLicenseDataModel.TABLE_NAME) as HashMap<String, Any?>
                dlFrontImageName =
                    hashMap[DrivingLicenseDataModel.KEY_NAME_FRONT_IMAGE] as String?
                dlBackImageName =
                    hashMap[DrivingLicenseDataModel.KEY_NAME_BACK_IMAGE] as String?
                userHasDL =
                    hashMap[DrivingLicenseDataModel.KEY_USER_HAS_DL] as Boolean?
                dlVerified =
                    hashMap[DrivingLicenseDataModel.KEY_NAME_VERIFIED] as Boolean
            } else {
                dlFrontImageName = null
                dlBackImageName = null
                userHasDL = null
                dlVerified = false
            }
        } catch (e: Exception) {
            dlFrontImageName = null
            dlBackImageName = null
            userHasDL = null
            dlVerified = false
        }

        return DrivingLicenseDataModel(
            userHasDL = userHasDL,
            frontImage = dlFrontImageName,
            backImage = dlBackImageName,
            verified = dlVerified
        )
    }

    private fun extractAadharInfo(
        aadharDetailsUploaded: Boolean,
        docSnap: DocumentSnapshot
    ): AadharCardDataModel {
        var aadharFrontImageName: String?
        var aadharBackImageName: String?
        var userHasAadhar: Boolean?
        var aadharVerified: Boolean

        try {

            if (aadharDetailsUploaded) {
                val hashMap =
                    docSnap.get(AadharCardDataModel.TABLE_NAME) as HashMap<String, Any?>
                aadharFrontImageName =
                    hashMap[AadharCardDataModel.KEY_NAME_FRONT_IMAGE] as String?
                aadharBackImageName =
                    hashMap[AadharCardDataModel.KEY_NAME_BACK_IMAGE] as String?
                userHasAadhar =
                    hashMap[AadharCardDataModel.KEY_NAME_USER_HAS_AADHAR] as Boolean?
                aadharVerified =
                    hashMap[AadharCardDataModel.KEY_NAME_VERIFIED] as Boolean
            } else {
                aadharFrontImageName = null
                aadharBackImageName = null
                userHasAadhar = null
                aadharVerified = false
            }
        } catch (e: Exception) {
            aadharFrontImageName = null
            aadharBackImageName = null
            userHasAadhar = null
            aadharVerified = false
        }

        return AadharCardDataModel(
            userHasAadharCard = userHasAadhar,
            frontImage = aadharFrontImageName,
            backImage = aadharBackImageName,
            verified = aadharVerified
        )
    }


    private fun extractBankDetailsInfo(
        bankDetailsUploaded: Boolean,
        docSnap: DocumentSnapshot
    ): BankDetailsDataModel {

        var passbookImageName: String?
        var userHasPassbook: Boolean?
        var passbookVerified: Boolean

        try {

            if (bankDetailsUploaded) {
                val hashMap =
                    docSnap.get(BankDetailsDataModel.TABLE_NAME) as HashMap<String, Any?>
                passbookImageName =
                    hashMap[BankDetailsDataModel.KEY_NAME_PASSBOOK_IMAGE_PATH] as String?
                userHasPassbook =
                    hashMap[BankDetailsDataModel.KEY_USER_HAS_PASSBOOK] as Boolean?
                passbookVerified =
                    hashMap[BankDetailsDataModel.KEY_NAME_VERIFIED] as Boolean
            } else {
                passbookImageName = null
                userHasPassbook = null
                passbookVerified = false
            }
        } catch (e: Exception) {
            passbookImageName = null
            userHasPassbook = null
            passbookVerified = false
        }

        return BankDetailsDataModel(
            userHasPassBook = userHasPassbook,
            passbookImagePath = passbookImageName,
            verified = passbookVerified
        )
    }


    fun updatePanImagePath(userhasPan: Boolean, panPath: String?) {
        gigerVerificationRepository.setDataAsKeyValue(
            PanCardDataModel(
                userHasPanCard = userhasPan,
                panCardImagePath = panPath,
                verified = false
            )
        )
    }

    fun updateBankPassbookImagePath(userHasPassBook: Boolean, passbookImagePath: String?) {
        gigerVerificationRepository.setDataAsKeyValue(
            BankDetailsDataModel(
                userHasPassBook = userHasPassBook,
                passbookImagePath = passbookImagePath,
                verified = false
            )
        )
    }

    fun updateAadharData(
        userHasAadhar: Boolean,
        frontImagePath: String?,
        backImagePath: String?
    ) {

        if (!userHasAadhar) {
            gigerVerificationRepository.setDataAsKeyValue(
                AadharCardDataModel(
                    userHasAadharCard = false,
                    verified = false
                )
            )
        } else {
            gigerVerificationRepository.getDBCollection().get().addOnSuccessListener {
                val containsAadharDocument = it?.contains(AadharCardDataModel.TABLE_NAME) ?: false

                if (!containsAadharDocument) {
                    gigerVerificationRepository.setDataAsKeyValue(
                        AadharCardDataModel(
                            userHasAadharCard = true,
                            frontImage = frontImagePath,
                            backImage = backImagePath,
                            verified = false
                        )
                    )
                } else {
                    val frontImage =
                        (it.get(AadharCardDataModel.TABLE_NAME) as HashMap<String, String?>).get(
                            AadharCardDataModel.KEY_NAME_FRONT_IMAGE
                        )

                    val backImage =
                        (it.get(AadharCardDataModel.TABLE_NAME) as HashMap<String, String?>).get(
                            AadharCardDataModel.KEY_NAME_BACK_IMAGE
                        )

                    val newFrontImage = frontImagePath ?: frontImage
                    val newBackImage = backImagePath ?: backImage

                    gigerVerificationRepository.setDataAsKeyValue(
                        AadharCardDataModel(
                            userHasAadharCard = true,
                            frontImage = newFrontImage,
                            backImage = newBackImage,
                            verified = false
                        )
                    )
                }
            }
        }
    }

    fun updateDLData(
        userHasDL: Boolean,
        frontImagePath: String?,
        backImagePath: String?
    ) {

        if (!userHasDL) {
            gigerVerificationRepository.setDataAsKeyValue(
                DrivingLicenseDataModel(
                    userHasDL = false,
                    verified = false
                )
            )
        } else {
            gigerVerificationRepository.getDBCollection().get().addOnSuccessListener {
                val containsDlDocument = it?.contains(DrivingLicenseDataModel.TABLE_NAME) ?: false

                if (!containsDlDocument) {
                    gigerVerificationRepository.setDataAsKeyValue(
                        DrivingLicenseDataModel(
                            userHasDL = true,
                            frontImage = frontImagePath,
                            backImage = backImagePath,
                            verified = false
                        )
                    )
                } else {
                    val frontImage =
                        (it.get(DrivingLicenseDataModel.TABLE_NAME) as HashMap<String, String?>).get(
                            DrivingLicenseDataModel.KEY_NAME_FRONT_IMAGE
                        )

                    val backImage =
                        (it.get(DrivingLicenseDataModel.TABLE_NAME) as HashMap<String, String?>).get(
                            DrivingLicenseDataModel.KEY_NAME_BACK_IMAGE
                        )

                    val newFrontImage = frontImagePath ?: frontImage
                    val newBackImage = backImagePath ?: backImage

                    gigerVerificationRepository.setDataAsKeyValue(
                        DrivingLicenseDataModel(
                            userHasDL = true,
                            frontImage = newFrontImage,
                            backImage = newBackImage,
                            verified = false
                        )
                    )
                }
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        verificationChangesListener?.remove()
    }
}