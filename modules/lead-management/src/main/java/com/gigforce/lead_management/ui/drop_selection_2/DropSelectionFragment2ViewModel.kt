package com.gigforce.lead_management.ui.drop_selection_2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropDetail
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropSelectionResponse
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DropSelectionFragment2ViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository
): ViewModel() {

    private val _submitDropSelectionState : MutableLiveData<Lce<DropSelectionResponse>> = MutableLiveData()
    val submitDropSelectionState : LiveData<Lce<DropSelectionResponse>> = _submitDropSelectionState

    fun dropSelections(
        selectionsToDrop : List<DropDetail>
    ) = viewModelScope.launch{

        try {
            _submitDropSelectionState.value = Lce.loading()
            val response = leadManagementRepository.dropSelections(selectionsToDrop)

            _submitDropSelectionState.value = Lce.content(response)
        } catch (e: Exception) {
            _submitDropSelectionState.value = Lce.error(e.message ?: "Unable to drop selections")
        }
    }
}