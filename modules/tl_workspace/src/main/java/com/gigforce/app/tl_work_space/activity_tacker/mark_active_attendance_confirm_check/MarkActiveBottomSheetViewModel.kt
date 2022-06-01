package com.gigforce.app.tl_work_space.activity_tacker.mark_active_attendance_confirm_check

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.app.tl_work_space.activity_tacker.AttendanceTLSharedViewModel
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

    private val _viewState = MutableStateFlow<MarkActiveViewContract.UiState>(
        MarkActiveViewContract.UiState.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    fun markPresent(
        gigId: String,
        sharedViewModel: AttendanceTLSharedViewModel
    ) = viewModelScope.launch {
        if (_viewState.value is MarkActiveViewContract.UiState.MarkingPresent) {
            logger.d(TAG, "already a decline process in progress, no-op")
            return@launch
        }

        _viewState.emit(MarkActiveViewContract.UiState.MarkingPresent)
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
            _viewState.emit(MarkActiveViewContract.UiState.PresentMarkedSuccessfully)

        } catch (e: Exception) {


            _viewState.emit(
                MarkActiveViewContract.UiState.ErrorWhileMarkingPresent(
                    e.message ?: "Unable to mark present"
                )
            )
        }
    }
}