package com.gigforce.app.tl_work_space.change_client_id

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gigforce.app.data.repositoriesImpl.tl_workspace.change_client_id.TLWorkspaceChangeClientIdRepository
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.BaseTLWorkSpaceViewModel
import com.gigforce.app.tl_work_space.TLWorkSpaceSharedViewModelEvent
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class ChangeClientIdViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val savedStateHandle: SavedStateHandle,
    private val repository: TLWorkspaceChangeClientIdRepository
) : BaseTLWorkSpaceViewModel<
        ChangeClientIdFragmentViewEvents,
        ChangeClientIdFragmentUiState,
        ChangeClientIdFragmentViewUiEffects>
    (
    initialState = ChangeClientIdFragmentUiState.ScreenInitializedOrRestored
) {

    companion object {
        private const val TAG = "ChangeClientIdViewModel"
    }

    private lateinit var existingClientId: String
    private lateinit var gigerId: String
    private lateinit var gigerName: String
    private lateinit var gigerMobile: String
    private lateinit var jobProfileId: String
    private lateinit var jobProfileName: String
    private lateinit var businessId: String

    private var newClientId: String? = null

    init {
        tryRestoringKeys()
    }

    private fun tryRestoringKeys() {
        existingClientId = savedStateHandle.get<String>(
            TLWorkSpaceNavigation.INTENT_EXTRA_EXISTING_CLIENT_ID
        ) ?: ""

        gigerId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF
        ) ?: return
        gigerName = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_NAME
        ) ?: return
        gigerMobile = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_MOBILE_NO
        ) ?: return

        jobProfileId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID
        ) ?: return
        jobProfileName = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_NAME
        ) ?: return

        businessId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID
        ) ?: return

    }

    fun setKeysReceivedFromPreviousScreen(
        existingClientId : String,
        gigerId: String,
        gigerMobile: String,
        gigerName: String,
        jobProfileId: String,
        jobProfileName: String,
        businessId: String
    ) {
        this.existingClientId = existingClientId
        this.gigerId = gigerId
        this.gigerMobile = gigerMobile
        this.gigerName = gigerName

        this.jobProfileId = jobProfileId
        this.jobProfileName = jobProfileName

        this.businessId = businessId

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_EXISTING_CLIENT_ID,
            existingClientId
        )

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_ID,
            gigerId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_NAME,
            gigerName
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_MOBILE_NO,
            gigerMobile
        )

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID,
            jobProfileId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_NAME,
            jobProfileName
        )

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_BUSINESS_ID,
            businessId
        )
    }


    override fun handleEvent(event: ChangeClientIdFragmentViewEvents) {
        when (event) {
            ChangeClientIdFragmentViewEvents.ChangeClientIdClicked -> submitClientId()
            is ChangeClientIdFragmentViewEvents.NewClientIdEntered -> {

                newClientId = event.reason
                checkForValidationAndEnableSubmitButton()
            }
        }
    }

    private fun checkForValidationAndEnableSubmitButton() {
        if (newClientId.isNullOrBlank() || existingClientId.equals(newClientId, true)) {

            setEffect {
                ChangeClientIdFragmentViewUiEffects.DisableSubmitButton
            }
            return
        }

        setEffect {
            ChangeClientIdFragmentViewUiEffects.EnableSubmitButton
        }
    }


    private fun submitClientId() = viewModelScope.launch {

        if (!validateClientId()) {
            return@launch
        }

        setState {
            ChangeClientIdFragmentUiState.ChangingClientId
        }

        try {
            repository.changeClientId(
                newClientId = newClientId!!,
                gigerId = gigerId,
                gigerMobile = gigerMobile,
                gigerName = gigerName,
                jobProfileId = jobProfileId,
                jobProfileName = jobProfileName,
                businessId = businessId
            )

            sharedViewModel.setEvent(
                TLWorkSpaceSharedViewModelEvent.ClientIdUpdatedOfGiger(
                    gigerId = gigerId,
                    newClientId = newClientId!!
                )
            )

            setState {
                ChangeClientIdFragmentUiState.ClientIdChanged
            }
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    ChangeClientIdFragmentUiState.ErrorWhileChangingClientId(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    ChangeClientIdFragmentUiState.ErrorWhileChangingClientId(
                        "Unable to load data"
                    )
                }
            }
        }
    }

    private fun validateClientId(): Boolean {

        if (newClientId.isNullOrBlank()) {

            setEffect {
                ChangeClientIdFragmentViewUiEffects.ClientIdValidationError(
                    "Client Id is empty"
                )
            }
            return false
        }

        if (existingClientId.equals(newClientId, true)) {

            setEffect {
                ChangeClientIdFragmentViewUiEffects.ClientIdValidationError(
                    "New Client Id is same as previous one"
                )
            }
            return false
        }

        return true
    }
}