package com.gigforce.giger_gigs.attendance_tl.attendance_details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.datamodels.attendance.GigAttendanceApiModel
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceStatus
import com.gigforce.common_ui.viewdatamodels.gig.AttendanceType
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.giger_gigs.models.GigAttendanceData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class GigerAttendanceDetailsViewModel @Inject constructor(
    private val logger: GigforceLogger,
    private val attendanceRepository: GigAttendanceRepository
) : ViewModel() {

    companion object {
        const val TAG = "PayoutDetailsViewModel"
    }

    private val _viewState = MutableStateFlow<GigerAttendanceDetailsViewContract.State>(
        GigerAttendanceDetailsViewContract.State.LoadingAttendanceDetails(null)
    )
    val viewState = _viewState.asStateFlow()

    private val _viewEffects = MutableSharedFlow<GigerAttendanceDetailsViewContract.UiEffect>()
    val viewEffects = _viewEffects.asSharedFlow()

    // Data
    private lateinit var gigId: String
    private var attendanceDetails: GigAttendanceData? = null

    fun handleEvent(
        event: GigerAttendanceDetailsViewContract.UiEvent
    ) = when (event) {
        GigerAttendanceDetailsViewContract.UiEvent.ActiveButtonClicked -> activeButtonClicked()
        GigerAttendanceDetailsViewContract.UiEvent.InactiveButtonClicked -> inActiveButtonClicked()
        GigerAttendanceDetailsViewContract.UiEvent.ResolveButtonClicked -> resolveButtonClicked()
        GigerAttendanceDetailsViewContract.UiEvent.AttendanceHistoryClicked -> attendanceHistoryClicked()
        GigerAttendanceDetailsViewContract.UiEvent.CallGigerButtonClicked -> callGigerClicked()
        GigerAttendanceDetailsViewContract.UiEvent.ChangeTLButtonClicked -> changeTLClicked()
        GigerAttendanceDetailsViewContract.UiEvent.DropGigerClicked -> dropGigerClicked()
    }

    private fun changeTLClicked() = viewModelScope.launch {

    }

    private fun attendanceHistoryClicked() = viewModelScope.launch {
        val gigDetails = attendanceDetails ?: return@launch
        attendanceDetails?.gigOrderId ?: return@launch

        _viewEffects.emit(
            GigerAttendanceDetailsViewContract.UiEffect.OpenMonthlyAttendanceScreen(
                gigOrderId = gigDetails.gigOrderId!!,
                date =gigDetails.gigDate,
                jobProfile = gigDetails.jobProfile,
                companyName = gigDetails.businessName,
                companyLogo = gigDetails.businessLogo
            )
        )
    }

    private fun dropGigerClicked() = viewModelScope.launch {

    }

    private fun callGigerClicked() = viewModelScope.launch {
        val gigerMobileNo = attendanceDetails?.gigerMobileNo ?: return@launch
        _viewEffects.emit(
            GigerAttendanceDetailsViewContract.UiEffect.CallGiger(
                gigerMobileNo
            )
        )
    }

    private fun activeButtonClicked() = viewModelScope.launch {
        val attendanceData = attendanceDetails ?: return@launch

        if (AttendanceType.OVERWRITE_BOTH == attendanceData.attendanceType) {

            _viewEffects.emit(
                GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerActiveScreen(
                    gigId = attendanceData.gigId,
                    hasGigerMarkedHimselfInActive = false
                )
            )
        } else {
            _viewEffects.emit(
                GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerActiveScreen(
                    gigId = attendanceData.gigId,
                    hasGigerMarkedHimselfInActive = attendanceData.gigerAttendanceStatus == AttendanceStatus.ABSENT
                )
            )
        }
    }

    private fun inActiveButtonClicked() = viewModelScope.launch {
        val attendanceData = attendanceDetails ?: return@launch

        if (AttendanceStatus.PRESENT == attendanceData.gigerAttendanceStatus) {

            _viewEffects.emit(
                GigerAttendanceDetailsViewContract.UiEffect.OpenMarkGigerInActiveConfirmationScreen(
                    gigId = attendanceData.gigId
                )
            )
        } else {

            _viewEffects.emit(
                GigerAttendanceDetailsViewContract.UiEffect.OpenSelectGigerInActiveReasonScreen(
                    gigId = attendanceData.gigId
                )
            )
        }
    }

    private fun resolveButtonClicked() = viewModelScope.launch {
        val attendanceData = attendanceDetails ?: return@launch

        _viewEffects.emit(
            GigerAttendanceDetailsViewContract.UiEffect.OpenResolveAttendanceScreen(
                gigId = attendanceData.gigId,
                attendanceData
            )
        )
    }

    fun setGigerAttendanceReceivedFromPreviousScreen(
        gigId: String
    ) = viewModelScope.launch {

        this@GigerAttendanceDetailsViewModel.gigId = gigId
        fetchAttendanceDetails(gigId)
    }

    private fun fetchAttendanceDetails(
        gigId: String
    ) = viewModelScope.launch {

        _viewState.emit(GigerAttendanceDetailsViewContract.State.LoadingAttendanceDetails(null))
        try {

            attendanceDetails = GigAttendanceData.fromGigAttendanceApiModel(
                attendanceRepository.getAttendanceDetails(
                    gigId
                )
            )

            _viewState.emit(
                GigerAttendanceDetailsViewContract.State.ShowAttendanceDetails(
                    attendanceDetails!!
                )
            )
        } catch (e: Exception) {

            if (e is IOException) {
                _viewState.emit(
                    GigerAttendanceDetailsViewContract.State.ErrorInLoadingAttendanceDetails(
                        e.message ?: "Unable to fetch attendance details"
                    )
                )
            } else {
                _viewState.emit(
                    GigerAttendanceDetailsViewContract.State.ErrorInLoadingAttendanceDetails(
                        "Unable to fetch attendance details"
                    )
                )
            }
        }
    }



    fun gigUpdateReceived(
        attendance: GigAttendanceApiModel
    ) = viewModelScope.launch {
        val updatedGigId = attendance.id ?: return@launch
        if(updatedGigId == gigId){

            attendanceDetails = GigAttendanceData.fromGigAttendanceApiModel(
                attendance
            )

            _viewState.emit(
                GigerAttendanceDetailsViewContract.State.ShowAttendanceDetails(
                    attendanceDetails!!
                )
            )
        }
    }
}