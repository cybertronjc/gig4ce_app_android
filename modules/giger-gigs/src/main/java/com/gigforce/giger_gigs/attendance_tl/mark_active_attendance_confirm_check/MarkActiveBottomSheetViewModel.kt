package com.gigforce.giger_gigs.attendance_tl.mark_active_attendance_confirm_check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarkActiveBottomSheetViewModel @Inject constructor(
    private val gigAttendanceRepository: GigAttendanceRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {

        const val TAG = "MarkActiveBottomSheetViewModel"
    }

    private val _viewState = MutableStateFlow<MarkInactiveReasonsViewContract.UiState>(
        MarkInactiveReasonsViewContract.UiState.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    fun markPresent(
        gigId: String,
        sharedViewModel: AttendanceTLSharedViewModel
    ) = viewModelScope.launch {
        if (_viewState.value is MarkInactiveReasonsViewContract.UiState.MarkingPresent) {
            logger.d(TAG, "already a decline process in progress, no-op")
            return@launch
        }

        _viewState.emit(MarkInactiveReasonsViewContract.UiState.MarkingPresent)
        try {
            val gigWithAttendanceUpdated = gigAttendanceRepository.markCheckIn(
                gigId = gigId,
                imagePathInFirebase = null,
                latitude = null,
                longitude = null,
                markingAddress = null,
                locationFake = null,
                locationAccuracy = null,
                distanceBetweenGigAndUser = null,
            )

            sharedViewModel.attendanceUpdated(gigWithAttendanceUpdated)
            _viewState.emit(MarkInactiveReasonsViewContract.UiState.PresentMarkedSuccessfully)

        } catch (e: Exception) {


            _viewState.emit(
                MarkInactiveReasonsViewContract.UiState.ErrorWhileMarkingPresent(
                    e.message ?: "Unable to mark present"
                )
            )
        }
    }
}