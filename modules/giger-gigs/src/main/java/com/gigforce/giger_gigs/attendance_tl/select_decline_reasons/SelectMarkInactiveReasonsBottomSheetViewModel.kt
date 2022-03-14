package com.gigforce.giger_gigs.attendance_tl.select_decline_reasons

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.gig.GigAttendanceRepository
import com.gigforce.core.base.BaseBottomSheetDialogFragment
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.giger_gigs.R
import com.gigforce.giger_gigs.attendance_tl.AttendanceTLSharedViewModel
import com.gigforce.giger_gigs.attendance_tl.GigAttendanceConstants
import com.gigforce.giger_gigs.databinding.FragmentMarkActiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentMarkInactiveConfirmationBinding
import com.gigforce.giger_gigs.databinding.FragmentSelectInactiveReasonBinding
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectMarkInactiveReasonsBottomSheetViewModel @Inject constructor(
    private val gigAttendanceRepository: GigAttendanceRepository,
    private val logger : GigforceLogger
) : ViewModel() {

    companion object{

        const val TAG = "DeclineGigViewModel"
    }

    private val _viewState = MutableLiveData<SelectMarkInactiveReasonsViewContract.UiState>()
    val viewState : LiveData<SelectMarkInactiveReasonsViewContract.UiState> = _viewState

    fun loadDeclineOptions(
        isUserTl : Boolean
    ) = viewModelScope.launch{

        _viewState.value = SelectMarkInactiveReasonsViewContract.UiState.LoadingDeclineOptions
        logger.d(TAG,"loading decline options UserTl : $isUserTl.....")

        try {

            val declineOptions = gigAttendanceRepository.getDeclineOptions(
                true
            ).filter {
                it.reasonId.isNotBlank()
            }

            _viewState.value = SelectMarkInactiveReasonsViewContract.UiState.ShowDeclineOptions(
                declineOptions
            )

            logger.d(TAG,"[Success] ${declineOptions.size} options loaded")
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


}