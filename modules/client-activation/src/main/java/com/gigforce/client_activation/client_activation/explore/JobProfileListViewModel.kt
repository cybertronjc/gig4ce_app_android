package com.gigforce.client_activation.client_activation.explore

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileRequestDataModel
import com.gigforce.client_activation.client_activation.repository.JobProfileRepository
import com.gigforce.common_ui.viewdatamodels.leadManagement.JobProfileOverview
import com.gigforce.core.di.interfaces.IBuildConfig
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.gigforce.core.logger.GigforceLogger
import com.gigforce.core.utils.Lce
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobProfileListViewModel @Inject constructor(private val iBuildConfigVM: IBuildConfigVM) : ViewModel() {

    companion object {
        private const val TAG = "JobProfileListViewModel"
    }

    private val jobProfileRepository = JobProfileRepository(iBuildConfigVM)
    private val logger = GigforceLogger()

    private val _viewState = MutableLiveData<Lce<List<JobProfileDVM>>>()
    val viewState: LiveData<Lce<List<JobProfileDVM>>> = _viewState

     fun getAllJobProfiles(requestData: JobProfileRequestDataModel) = viewModelScope.launch {
        _viewState.postValue(Lce.loading())

        try {
            logger.d(TAG, "fetching job profiles...")

            val jobProfiles = jobProfileRepository.getJobProfiles(
                requestData
            )

            _viewState.value = Lce.content(jobProfiles)

            logger.d(TAG, "received ${jobProfiles.size} job profiles from server")

        } catch (e: Exception) {
            _viewState.value = Lce.error("Unable to load Job Profiles")
            logger.e(
                TAG,
                " getAllJobProfiles()",
                e
            )
        }
    }

}