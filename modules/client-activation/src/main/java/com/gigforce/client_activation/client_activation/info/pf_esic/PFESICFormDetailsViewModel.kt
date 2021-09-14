package com.gigforce.client_activation.client_activation.info.pf_esic

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.core.datamodels.profile.PFESICDataModel
import com.gigforce.client_activation.client_activation.repository.PFESICDetailsRepository
import com.gigforce.common_ui.repository.ProfileFirebaseRepository
import com.gigforce.core.di.interfaces.IBuildConfigVM
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PFESICFormDetailsViewModel @Inject constructor(
    private val iBuildConfigVM: IBuildConfigVM,
    private val profileFirebaseRepository: ProfileFirebaseRepository
) : ViewModel() {

    var uid = FirebaseAuth.getInstance().currentUser?.uid!!
    val pfEsicRepository = PFESICDetailsRepository()

    val pfEsicResult: MutableLiveData<PFESICDataModel> = MutableLiveData<PFESICDataModel>()

    val pfEsicSumbitResult: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    fun getDetails() = viewModelScope.launch {
        try {
            val pfEsic = profileFirebaseRepository.getProfileData(uid)?.pfesic
            pfEsicResult.postValue(pfEsic)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    fun setDetails(pfesicDetails: PFESICDataModel) = viewModelScope.launch {
        try {
            val pfEsic = pfEsicRepository.submitData(pfesicDetails)
            pfEsicSumbitResult.postValue(pfEsic)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}