package com.gigforce.lead_management.ui.reference_check

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.datamodels.profile.Reference
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferenceCheckViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    companion object {
        const val TAG = "ReferenceCheckViewModel"
    }

    private val _viewState: MutableLiveData<ReferenceCheckViewState> = MutableLiveData()
    val viewState: LiveData<ReferenceCheckViewState> = _viewState

    //Data
    private var userReferenceCheckData: Reference? = null

    fun fetchPreviousReferenceCheckData(
        userUid: String
    ) = viewModelScope.launch {
        if (userReferenceCheckData != null) {

            logger.d(TAG, "cached reference data found")
            _viewState.value = ReferenceCheckViewState.PreviousReferenceDataFetched(
                referenceData = userReferenceCheckData!!
            )
            return@launch
        }

        _viewState.value = ReferenceCheckViewState.FetchingReferenceDataFromProfile
        try {
            logger.d(TAG, "fetching reference data for user , ${userUid}......")

            userReferenceCheckData = profileFirebaseRepository.getProfileData(
                userId = userUid
            ).reference ?: Reference()

            _viewState.value = ReferenceCheckViewState.PreviousReferenceDataFetched(
                referenceData = userReferenceCheckData ?: Reference()
            )
            logger.d(TAG, "[Success] fetched reference data")
        } catch (e: Exception) {
            userReferenceCheckData = Reference()
            _viewState.value = ReferenceCheckViewState.ErrorWhileFetchingPreviousReferenceData(
                error = "Error while fetching previous reference data"
            )

            logger.e(TAG, "[Failure] fetching users refernce data", e)
        }
    }

    fun handleEvent(
        referenceCheckEvent: ReferenceCheckEvent
    ) = when (referenceCheckEvent){
        is ReferenceCheckEvent.ContactNoChanged -> {
            userReferenceCheckData?.contactNo = referenceCheckEvent.contactNo
        }
        is ReferenceCheckEvent.NameChanged -> {
            userReferenceCheckData?.name = referenceCheckEvent.name
        }
        is ReferenceCheckEvent.RelationChanged -> {
            userReferenceCheckData?.relation = referenceCheckEvent.relation
        }
        is ReferenceCheckEvent.SubmitButtonPressed -> {
            saveReference(
                referenceCheckEvent.userUid,
                referenceCheckEvent.name,
                referenceCheckEvent.relation,
                referenceCheckEvent.contactNo
            )
        }
    }

    fun saveReference(
        userUid: String,
        name : String,
        relation: String,
        contactNo : String
    ) = viewModelScope.launch {

        if (checkIfDataValid(
                name,
                relation,
                contactNo
            ).not()
        ) {
            logger.d(TAG, "validation failed")
            return@launch
        }

        _viewState.postValue(
            ReferenceCheckViewState.SubmittingReferenceData
        )
        logger.d(
            tag = TAG,
            message = "saving reference data....",
            mapOf(
                "name" to name,
                "relation" to relation,
                "contact_no" to contactNo
            )
        )

        try {
            leadManagementRepository.saveReference(
                userUid = userUid,
                name = name,
                relation = relation,
                contactNo = contactNo
            )
            logger.d(
                tag = TAG,
                message = "relation submitted"
            )

            _viewState.postValue(
                ReferenceCheckViewState.SubmittingReferenceDataSuccess
            )
        } catch (e: Exception) {
            _viewState.postValue(
                ReferenceCheckViewState.SubmittingReferenceDataError(
                    error = "Unable to submit reference",
                    shouldShowErrorButton = false
                )
            )

            logger.e(
                TAG,
                "submitting reference",
                e
            )
        }
    }

    private fun checkIfDataValid(
        name: String,
        relation: String,
        contactNo: String
    ): Boolean {

        return when {
            name.isEmpty() -> {
                _viewState.postValue(
                    ReferenceCheckViewState.ValidationError(
                        nameValidationError = "Invalid name"
                    )
                )
                logger.d(TAG, "invalid name provided : $name")
                false
            }
            relation.isEmpty() -> {
                _viewState.postValue(
                    ReferenceCheckViewState.ValidationError(
                        relationValidationError = "Invalid relation"
                    )
                )

                logger.d(TAG, "invalid relation provided : $relation")
                false
            }
            contactNo.isEmpty() -> {
                _viewState.postValue(
                    ReferenceCheckViewState.ValidationError(
                        contactValidationError = "Invalid Contact no"
                    )
                )

                logger.d(TAG, "invalid contactNo provided : $contactNo")
                false
            }
            else -> {
                true
            }
        }
    }
}