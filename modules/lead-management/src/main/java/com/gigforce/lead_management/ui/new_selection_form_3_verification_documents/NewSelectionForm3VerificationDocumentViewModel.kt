package com.gigforce.lead_management.ui.new_selection_form_3_verification_documents

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.dynamic_fields.data.DynamicVerificationField
import com.gigforce.common_ui.dynamic_fields.data.FieldTypes
import com.gigforce.common_ui.dynamic_fields.data.VerificationStatus
import com.gigforce.common_ui.repository.GigerVerificationRepository
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.*
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.models.WhatsappTemplateModel
import com.gigforce.lead_management.viewModels.JoiningSubmissionViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewSelectionForm3VerificationDocumentViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val profileFirebaseRepository: ProfileFirebaseRepository,
    private val verificationRepository: GigerVerificationRepository,
    private val logger: GigforceLogger
) : JoiningSubmissionViewModel(
    leadManagementRepository,
    logger
) {

    companion object {
        private const val TAG = "NewSelectionForm3VerificationDocumentViewModel"
    }

    private val _uiEffects = MutableSharedFlow<NewSelectionForm3UiEffects>()
    val uiEffects = _uiEffects.asSharedFlow()

    private val _viewState = MutableLiveData<NewSelectionForm3ViewState>()
    val viewState: LiveData<NewSelectionForm3ViewState> = _viewState

    private var userDocumentListenerRegistration: ListenerRegistration? = null

    //Data from previous screen
    private var currentlySubmittingAnyJoiningRequuest: Boolean = false
    private lateinit var joiningRequest: SubmitJoiningRequest
    private lateinit var userUid: String
    private var verificationDynamicFields: List<DynamicVerificationField> = emptyList()

    override fun onCleared() {
        super.onCleared()
        userDocumentListenerRegistration?.remove()
        logger.d(TAG,"removing verification snapshot listener ....")
    }

    fun handleEvent(
        event: NewSelectionForm3Events
    ) {
        when (event) {
            NewSelectionForm3Events.SubmitButtonPressed -> submitJoiningData(joiningRequest)
            is NewSelectionForm3Events.RequiredVerificationDocumentListAcquiredFromPreviousPage -> {

                joiningRequest = event.joiningRequest
                verificationDynamicFields = event.requiredVerificationDocument
                userUid = event.userUid

                checkVerificationDocumentStatusAndShowIn()
            }
        }
    }


    private fun checkVerificationDocumentStatusAndShowIn() = viewModelScope.launch {
        _viewState.value = NewSelectionForm3ViewState.CheckingVerificationDocumentsStatus
        logger.d(TAG,"Starting to listen to verification document of user $userUid")

        userDocumentListenerRegistration = verificationRepository.verificationDocumentReference(
            userUid
        ).addSnapshotListener { value, error ->

            value?.let {
                processVerificationChange(it)
            }

            error?.let {
                logger.e(TAG,"ERROR in listening to verification document of user $userUid",it)

                _viewState.value = NewSelectionForm3ViewState.ShowVerificationDocumentFields(
                    verificationDynamicFields
                )
            }
        }
    }

    private fun processVerificationChange(
        it: DocumentSnapshot
    ) = viewModelScope.launch {
        val verificationDocument = if(it.exists()) {
            it.toObject(VerificationBaseModel::class.java)
                ?: VerificationBaseModel()
        } else{
            logger.d(TAG,"[Important] Verification document doesn't exist ")
            VerificationBaseModel()
        }


        val userUploadedAadhaarCard =
            verificationDocument.aadhaar_card_questionnaire?.aadhaarCardNo != null

        logger.d(TAG,"Aadhaar Filled : ${verificationDocument.aadhaar_card_questionnaire?.aadhaarCardNo != null}")
        logger.d(TAG,"Bank status : ${verificationDocument.bank_details?.status}")
        logger.d(TAG,"Driving status : ${verificationDocument.driving_license?.status}")
        logger.d(TAG,"PAN status : ${verificationDocument.pan_card?.status}")

        verificationDynamicFields.onEach {
            it.userId = userUid

            if (it.fieldType == FieldTypes.AADHAAR_VERIFICATION_VIEW) {
                it.status = if (userUploadedAadhaarCard) VerificationStatus.VERIFIED else VerificationStatus.NOT_UPLOADED
            } else if (it.fieldType == FieldTypes.BANK_VERIFICATION_VIEW) {
                it.status = VerificationStatus.getStatusStringFromServerString(verificationDocument.bank_details?.status)
            } else if (it.fieldType == FieldTypes.DL_VERIFICATION_VIEW) {
                it.status = VerificationStatus.getStatusStringFromServerString(verificationDocument.driving_license?.status)
            } else if (it.fieldType == FieldTypes.PAN_VERIFICATION_VIEW) {
                it.status = VerificationStatus.getStatusStringFromServerString(verificationDocument.pan_card?.status)
            }
        }

        logger.d(TAG,"Updating Status views Verification item list: $verificationDynamicFields")
        _viewState.value = NewSelectionForm3ViewState.ShowVerificationDocumentFields(
            verificationDynamicFields
        )

        if (checkIfAnyRequiredDocumentNotUploaded()) {
            _uiEffects.emit(NewSelectionForm3UiEffects.DisableSubmitButton)
        } else {
            _uiEffects.emit(NewSelectionForm3UiEffects.EnableSubmitButton)
        }
    }

    private fun checkIfAnyRequiredDocumentNotUploaded() = verificationDynamicFields.find {
       it.mandatory &&  it.status == VerificationStatus.NOT_UPLOADED
    } != null


    private fun submitJoiningData(
        joiningRequest: SubmitJoiningRequest
    ) = viewModelScope.launch {

        if (currentlySubmittingAnyJoiningRequuest) {
            logger.d(
                TAG,
                "Already a joining request submission in progress, ignoring this one",
            )
            return@launch
        }

        currentlySubmittingAnyJoiningRequuest = true

        try {
            _viewState.value = NewSelectionForm3ViewState.SubmittingJoiningData

            val shareLink = cleanUpJoiningDataAndSubmitJoiningData(
                joiningRequest
            )
            currentlySubmittingAnyJoiningRequuest = false

            _viewState.value = NewSelectionForm3ViewState.JoiningDataSubmitted(
                WhatsappTemplateModel(
                    shareLink = shareLink,
                    businessName = joiningRequest.business.name ?: "",
                    tlName = "",
                    jobProfileName = joiningRequest.jobProfile.name ?: "",
                    tlMobileNumber = ""
                )
            )
        } catch (e: Exception) {
            currentlySubmittingAnyJoiningRequuest = false

            _viewState.value = NewSelectionForm3ViewState.ErrorWhileSubmittingJoiningData(
                error = e.message ?: "Unable to submit joining request, please try again later",
                shouldShowErrorButton = false
            )
            _viewState.value = null
        }
    }
}