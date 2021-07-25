package com.gigforce.lead_management.ui.reference_check

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.viewdatamodels.client_activation.JobProfile
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.lead_management.repositories.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReferenceCheckViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {
        const val TAG = "ReferenceCheckViewModel"
    }

    private val _viewState: MutableLiveData<ReferenceCheckViewState> = MutableLiveData()
    val viewState: LiveData<ReferenceCheckViewState> = _viewState

    fun saveReference(
        userUid : String,
        name: String,
        relation: String,
        contactNo: String
    ) = viewModelScope.launch {

        JobProfile

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
                contactNo = contactNo,
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