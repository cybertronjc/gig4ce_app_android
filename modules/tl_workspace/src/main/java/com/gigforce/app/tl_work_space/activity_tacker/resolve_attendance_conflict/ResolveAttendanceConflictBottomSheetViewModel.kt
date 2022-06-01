package com.gigforce.app.tl_work_space.activity_tacker.resolve_attendance_conflict

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
class ResolveAttendanceConflictBottomSheetViewModel @Inject constructor(
    private val gigAttendanceRepository: GigAttendanceRepository,
    private val logger: GigforceLogger
) : ViewModel() {

    companion object {

        const val TAG = "MarkActiveBottomSheetViewModel"
    }

    private val _viewState = MutableStateFlow<ResolveAttendanceConflictViewContract.UiState>(
        ResolveAttendanceConflictViewContract.UiState.ScreenLoaded
    )
    val viewState = _viewState.asStateFlow()

    fun resolveConflict(
        resolveId: String,
        optionSelected: Boolean,
        sharedViewModel: AttendanceTLSharedViewModel
    ) = viewModelScope.launch {
        if (_viewState.value is ResolveAttendanceConflictViewContract.UiState.ResolvingConflict) {
            logger.d(TAG, "already a decline process in progress, no-op")
            return@launch
        }

        _viewState.emit(
            ResolveAttendanceConflictViewContract.UiState.ResolvingConflict(
                optionSelected
            )
        )
        try {
            val gigWithAttendanceUpdated = gigAttendanceRepository.resolveAttendanceConflict(
                resolveId = resolveId,
                optionSelected = optionSelected
            )

            sharedViewModel.attendanceUpdated(gigWithAttendanceUpdated)
            _viewState.emit(ResolveAttendanceConflictViewContract.UiState.ConflictResolvedSuccessfully)

        } catch (e: Exception) {


            _viewState.emit(
                ResolveAttendanceConflictViewContract.UiState.ErrorWhileResolvingConflict(
                    e.message ?: "Unable to mark present"
                )
            )
        }
    }
}