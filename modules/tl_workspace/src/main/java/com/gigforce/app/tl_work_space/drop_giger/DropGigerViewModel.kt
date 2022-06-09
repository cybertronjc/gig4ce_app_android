package com.gigforce.app.tl_work_space.drop_giger

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger.TLWorkspaceDropGigerRepository
import com.gigforce.app.navigation.tl_workspace.TLWorkSpaceNavigation
import com.gigforce.app.tl_work_space.drop_giger.models.DropOption
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DropGigerViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val savedStateHandle: SavedStateHandle,
    private val repository: TLWorkspaceDropGigerRepository
) : BaseViewModel<
        DropGigerFragmentViewEvents,
        DropGigerFragmentUiState,
        DropGigerFragmentViewUiEffects>
    (
    initialState = DropGigerFragmentUiState.LoadingDropOptionsData
) {

    companion object {
        private const val TAG = "DropGigerViewModel"
    }

    private var dropOptions = listOf<DropOption>()

    private lateinit var jobProfileId: String
    private lateinit var gigerId: String

    private var lastWorkingDate: LocalDate? = null
    private var selectedReason: DropOption? = null
    private var customReasonString: String? = null

    init {
        tryRestoringKeys()
        getDropOptions()
    }

    private fun tryRestoringKeys() {
        gigerId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_OPEN_USER_DETAILS_OF
        ) ?: return
        jobProfileId = savedStateHandle.get<String?>(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID
        ) ?: return
    }

    fun setKeysReceivedFromPreviousScreen(
        gigerId: String,
        jobProfileId: String
    ) {
        this.gigerId = gigerId
        this.jobProfileId = jobProfileId

        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_GIGER_ID,
            gigerId
        )
        savedStateHandle.set(
            TLWorkSpaceNavigation.INTENT_EXTRA_JOB_PROFILE_ID,
            jobProfileId
        )
    }

    private fun getDropOptions() = viewModelScope.launch {

        setState {
            DropGigerFragmentUiState.LoadingDropOptionsData
        }

        try {
            dropOptions = repository
                .getDropOptions()
                .map {

                    DropOption(
                        dropLocalizedText = it.dropLocalizedText,
                        reasonId = it.reasonId,
                        customReason = it.customReason,
                        selected = it.reasonId == selectedReason?.reasonId,
                        viewModel = this@DropGigerViewModel
                    )
                }

            setState {

                DropGigerFragmentUiState.ShowOptionsData(
                    dropOptions
                )
            }
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    DropGigerFragmentUiState.ErrorWhileLoadingDropOptions(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    DropGigerFragmentUiState.ErrorWhileLoadingDropOptions(
                        "Unable to load data"
                    )
                }
            }
        }
    }


    override fun handleEvent(event: DropGigerFragmentViewEvents) {
        when (event) {
            DropGigerFragmentViewEvents.DropButtonClicked -> dropGiger()
            is DropGigerFragmentViewEvents.LastWorkingDateSelected -> lastWorkingDateEdited(
                event.date
            )
            is DropGigerFragmentViewEvents.ReasonSelected -> reasonSelected(
                event.reason
            )
            is DropGigerFragmentViewEvents.CustomReasonEntered -> customReasonEntered(
                event.reason
            )
        }
    }

    private fun reasonSelected(reason: DropOption) {
        this.selectedReason = reason
        checkForValidationAndEnableSubmitButton()

        dropOptions.onEach {
            it.selected = it.reasonId == selectedReason?.reasonId
        }

        setState {

            DropGigerFragmentUiState.ShowOptionsData(
                dropOptions
            )
        }


        if (reason.customReason) {

            setEffect {
                DropGigerFragmentViewUiEffects.ShowCustomReasonLayout
            }
        } else {

            setEffect {
                DropGigerFragmentViewUiEffects.HideCustomReasonLayout
            }
        }
    }

    private fun lastWorkingDateEdited(date: LocalDate) {
        this.lastWorkingDate = date
        checkForValidationAndEnableSubmitButton()
    }

    private fun customReasonEntered(reason: String) {
        this.customReasonString = reason
        checkForValidationAndEnableSubmitButton()
    }

    private fun checkForValidationAndEnableSubmitButton() {
        if (lastWorkingDate == null) {
            setEffect {
                DropGigerFragmentViewUiEffects.DisableSubmitButton
            }
            return
        }

        if (selectedReason == null ||
            (selectedReason!!.customReason &&
                    customReasonString.isNullOrBlank())
        ) {

            setEffect {
                DropGigerFragmentViewUiEffects.DisableSubmitButton
            }
            return
        }

        setEffect {
            DropGigerFragmentViewUiEffects.EnableSubmitButton
        }
    }


    private fun dropGiger() = viewModelScope.launch {
        val finalReason = selectedReason ?: return@launch

        setState {
            DropGigerFragmentUiState.DroppingGiger
        }

        try {
            repository.dropGiger(
                gigerId = gigerId,
                jobProfileId = jobProfileId,
                customReason = finalReason.customReason,
                reasonId = finalReason.reasonId,
                reasonText = if (finalReason.customReason) customReasonString!! else finalReason.dropLocalizedText,
                lastWorkingDate = lastWorkingDate!!
            )

            setState {
                DropGigerFragmentUiState.GigerDroppedWithSuccess
            }
        } catch (e: Exception) {

            if (e is IOException) {
                setState {
                    DropGigerFragmentUiState.ErrorWhileDroppingGiger(
                        e.message ?: "Unable to load data"
                    )
                }
            } else {

                setState {
                    DropGigerFragmentUiState.ErrorWhileDroppingGiger(
                        "Unable to load data"
                    )
                }
            }
        }
    }
}