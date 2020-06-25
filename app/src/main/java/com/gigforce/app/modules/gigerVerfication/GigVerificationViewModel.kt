package com.gigforce.app.modules.gigerVerfication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.gigerVerfication.aadharCard.AadharCardDataModel
import com.gigforce.app.modules.gigerVerfication.bankDetails.BankDetailsDataModel
import com.gigforce.app.modules.gigerVerfication.drivingLicense.DrivingLicenseDataModel
import com.gigforce.app.modules.gigerVerfication.panCard.PanCardDataModel
import com.gigforce.app.modules.gigerVerfication.selfieVideo.SelfieVideoDataModel
import com.google.firebase.firestore.ListenerRegistration

data class GigerVerificationStatus(
    val selfieVideoUploaded: Boolean,
    val panCardDetailsUploaded: Boolean,
    val panCardImageName: String? = null,
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

                val selfieUploaded = docSnap.contains(SelfieVideoDataModel.TABLE_NAME)

                val panDetailsUploaded = docSnap.contains(PanCardDataModel.TABLE_NAME)
                val panCardImageName =
                    try {

                        if (panDetailsUploaded)
                            (docSnap.get(PanCardDataModel.TABLE_NAME) as HashMap<String, Any?>)[PanCardDataModel.KEY_NAME_PASSBOOK_IMAGE_PATH] as String?
                        else
                            null
                    } catch (e: Exception) {
                        null
                    }

                val dlDetailsUploaded =
                    docSnap.contains(DrivingLicenseDataModel.TABLE_NAME)

                val aadharCardDetailsUploaded =
                    docSnap.contains(AadharCardDataModel.TABLE_NAME)

                val bankDetailsUploaded =
                    docSnap.contains(BankDetailsDataModel.TABLE_NAME)

                val everyDocumentUploaded = selfieUploaded
                        && panDetailsUploaded
                        && dlDetailsUploaded
                        && aadharCardDetailsUploaded
                        && bankDetailsUploaded

                _gigerVerificationStatus.value = GigerVerificationStatus(
                    selfieVideoUploaded = selfieUploaded,
                    panCardDetailsUploaded = panDetailsUploaded,
                    panCardImageName = panCardImageName,
                    aadharCardDetailsUploaded = aadharCardDetailsUploaded,
                    dlCardDetailsUploaded = dlDetailsUploaded,
                    bankDetailsUploaded = bankDetailsUploaded,
                    everyDocumentUploaded = everyDocumentUploaded
                )
            }
    }


    fun updatePanImagePath(panPath: String) {
        gigerVerificationRepository.setDataAsKeyValue(
            PanCardDataModel(
                panCardImagePath = panPath,
                verified = false
            )
        )
    }

    fun updateBankPassbookImagePath(passbookImagePath: String) {
        gigerVerificationRepository.setDataAsKeyValue(
            BankDetailsDataModel(
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