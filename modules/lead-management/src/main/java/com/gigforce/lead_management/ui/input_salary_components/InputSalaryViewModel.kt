package com.gigforce.lead_management.ui.input_salary_components

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.common_ui.repository.LeadManagementRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.InputSalaryResponse
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileDetails
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputSalaryViewModel @Inject constructor(
    private val leadManagementRepository: LeadManagementRepository,
    private val logger: GigforceLogger,
    private val firebaseAuthStateListener: FirebaseAuthStateListener,
) : ViewModel() {

    companion object {
        private const val TAG = "InputSalaryViewModel"
    }

    private val _viewState = MutableLiveData<Lce<InputSalaryResponse>>()
    val viewState: LiveData<Lce<InputSalaryResponse>> = _viewState

    fun getSalaryComponents(businessId: String, type: String) = viewModelScope.launch {
        _viewState.postValue(Lce.loading())

        try {
            logger.d(TAG, "fetching salary components...")

            val salaryComponents = leadManagementRepository.getSalaryComponents(
                businessId,
                type = type
            )
            _viewState.value = Lce.content(salaryComponents)

            logger.d(TAG, "received ${salaryComponents} salary Components from server")

        } catch (e: Exception) {
            _viewState.value = Lce.error("Unable to load Salary Components")
            logger.e(
                TAG,
                " getSalaryComponents()",
                e
            )
        }
    }

}