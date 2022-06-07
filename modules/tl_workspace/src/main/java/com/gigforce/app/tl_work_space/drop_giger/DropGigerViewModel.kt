package com.gigforce.app.tl_work_space.drop_giger

import androidx.lifecycle.viewModelScope
import com.gigforce.app.android_common_utils.base.viewModel.BaseViewModel
import com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger.DropOption
import com.gigforce.app.data.repositoriesImpl.tl_workspace.drop_giger.TLWorkspaceDropGigerRepository
import com.gigforce.core.logger.GigforceLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.IOException
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class DropGigerViewModel @Inject constructor(
    private val logger: GigforceLogger,
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

        private const val DROP_OPTION_ID_OTHER = "other"
    }

    private lateinit var jobProfileId: String
    private lateinit var gigerId: String

    private var lastWorkingDate: LocalDate? = null
    private var selectedReason: DropOption? = null

    init {
        getDropOptions()
    }

    private fun getDropOptions() = viewModelScope.launch {


        setState {
            DropGigerFragmentUiState.LoadingDropOptionsData
        }

        try {
            val dropOptions = repository.getDropOptions().toMutableList().apply {
                add(
                    DropOption(
                        "Other",
                        reasonId = DROP_OPTION_ID_OTHER,
                        customReason = true
                    )
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
    }

    private fun lastWorkingDateEdited(date: LocalDate) {
        this.lastWorkingDate = date
        checkForValidationAndEnableSubmitButton()
    }

    private fun customReasonEntered(reason: String) {
        this.selectedReason?.reasonId = reason
        this.selectedReason?.dropLocalizedText = reason
        checkForValidationAndEnableSubmitButton()
    }

    private fun checkForValidationAndEnableSubmitButton() {
        TODO("Not yet implemented")
    }


    private fun dropGiger() = viewModelScope.launch {

        setState {
            DropGigerFragmentUiState.DroppingGiger
        }

        try {
            repository.dropGiger(
                gigerId = gigerId,
                jobProfileId = jobProfileId,
                reasonId = selectedReason!!.reasonId,
                reasonText = selectedReason!!.dropLocalizedText,
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