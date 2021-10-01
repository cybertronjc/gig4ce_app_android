package com.gigforce.lead_management.ui.drop_selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.utils.Lse
import com.gigforce.common_ui.repository.LeadManagementRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DropSelectionViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository
): ViewModel() {

    private val _submitDropSelectionState : MutableLiveData<Lse> = MutableLiveData()
    val submitDropSelectionState : LiveData<Lse> = _submitDropSelectionState

    fun dropSelections(
        selectionsToDrop : ArrayList<String>
    ) = viewModelScope.launch{

        try {
            _submitDropSelectionState.value = Lse.loading()
            leadManagementRepository.dropSelections(selectionsToDrop)

            _submitDropSelectionState.value = Lse.success()
        } catch (e: Exception) {
            _submitDropSelectionState.value = Lse.error(e.message ?: "Unable to drop selections")
        }
    }
}