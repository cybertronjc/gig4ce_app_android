package com.gigforce.app.tl_work_space.activity_tacker.mark_inactive_attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class SelectMarkInactiveReasonsBottomSheetViewModel @Inject constructor(
    private val gigAttendanceRepository: GigAttendanceRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {

        const val TAG = "DeclineGigViewModel"
    }

    private val _viewState = MutableStateFlow<SelectMarkInactiveReasonsViewContract.UiState>(
        SelectMarkInactiveReasonsViewContract.UiState.LoadingDeclineOptions
    )
    val viewState = _viewState.asStateFlow()

    init {
        loadDeclineOptions(false)
    }

    fun loadDeclineOptions(
        isUserTl: Boolean
    ) = viewModelScope.launch {

        _viewState.value = SelectMarkInactiveReasonsViewContract.UiState.LoadingDeclineOptions
        logger.d(TAG, "loading decline options UserTl : $isUserTl.....")

        try {

            val declineOptions = gigAttendanceRepository.getDeclineOptions(
                true
            ).filter {
                it.reasonId.isNotBlank()
            }

            _viewState.value = SelectMarkInactiveReasonsViewContract.UiState.ShowDeclineOptions(
                declineOptions
            )

            logger.d(TAG, "[Success] ${declineOptions.size} options loaded")
        } catch (e: Exception) {

            //load default reasons here
//            _viewState.value = SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileLoadingDeclineOptions(
//                e.message ?: "Unable to fetch decline options"
//            )

            logger.e(
                TAG,
                "[Failed] error while loading decline options",
                e
            )
        }
    }


    fun markDecline(
        gigId: String,
        reasonId: String,
        reason: String,
        sharedViewModel: AttendanceTLSharedViewModel
    ) = viewModelScope.launch {
        if (_viewState.value is SelectMarkInactiveReasonsViewContract.UiState.MarkingDecline) {
            logger.d(TAG, "already a decline process in progress, no-op")
            return@launch
        }

        _viewState.emit(SelectMarkInactiveReasonsViewContract.UiState.MarkingDecline)
        try {
            val gigWithAttendanceUpdated = gigAttendanceRepository.markDecline(
                gigId = gigId,
                reasonId = reasonId,
                reason = reason
            )

            sharedViewModel.attendanceUpdated(gigWithAttendanceUpdated)
            _viewState.emit(SelectMarkInactiveReasonsViewContract.UiState.DeclineMarkedSuccessfully)

        } catch (e: Exception) {
            if(e is IOException){

                //Internet Exception
                _viewState.emit(
                    SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileMarkingDecline(
                        e.message ?: "Unable to mark decline"
                    )
                )
            } else{

                _viewState.emit(
                    SelectMarkInactiveReasonsViewContract.UiState.ErrorWhileMarkingDecline(
                        e.message ?: "Unable to mark decline"
                    )
                )
            }
        }
    }
}