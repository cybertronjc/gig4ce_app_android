package com.gigforce.giger_app.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.core.datamodels.verification.BankDetailsDataModel
import com.gigforce.core.datamodels.verification.VerificationBaseModel
import com.gigforce.giger_app.repo.IHomeCardsFBRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val homeCardsFBRepository: IHomeCardsFBRepository
) : ViewModel() {
    private var allLandingData: MutableLiveData<List<Any>> = MutableLiveData<List<Any>>()

    var _allLandingData: LiveData<List<Any>> = allLandingData

    private val _bankDetailedObject = MutableLiveData<BankDetailsDataModel>()
    val bankDetailedObject: LiveData<BankDetailsDataModel> = _bankDetailedObject

    init {
        homeCardsFBRepository.getData().observeForever {
            it?.let {
                this.allLandingData.value = it
            }
        }
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseFirestore.getInstance()
                .collection("Verification").document(it)
                .addSnapshotListener { value, error ->
                    value?.data?.let {
                        val doc = value.toObject(VerificationBaseModel::class.java)
                        doc?.bank_details?.let {
                            _bankDetailedObject.value = it
                        }
                    }
                }
        }


    }
}