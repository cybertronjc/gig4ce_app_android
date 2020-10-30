package com.gigforce.app.modules.wallet

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.wallet.models.Payslip
import com.gigforce.app.utils.Lce
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class PayslipMonthlyViewModel constructor(
    private val walletRepository: WalletfirestoreRepository = WalletfirestoreRepository(),
    private var profileFirebaseRepository: ProfileFirebaseRepository = ProfileFirebaseRepository()
) : ViewModel() {

    init {
        getProfileData()
    }

    private var profileListenerRegistration : ListenerRegistration? = null

    private val _userProfileData: MutableLiveData<ProfileData> = MutableLiveData()
    var userProfileData: LiveData<ProfileData> = _userProfileData

    fun getProfileData() {
        profileListenerRegistration = profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    Log.w("ProfileViewModel", "Listen failed", e)
                    return@EventListener
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value.data.toString())
                    _userProfileData.postValue(
                        value.toObject(ProfileData::class.java)
                    )
                    Log.d("ProfileViewModel", userProfileData.toString())
                }
            })
    }


    private val _monthlySlips = MutableLiveData<Lce<List<Payslip>>>()
    val monthlySlips: LiveData<Lce<List<Payslip>>> = _monthlySlips

    fun getPaySlips() = viewModelScope.launch {
        _monthlySlips.value = Lce.loading()

        try {
            val paySlips = walletRepository.getPaySlips()
            _monthlySlips.value = Lce.content(paySlips)
        } catch (e: Exception) {
            _monthlySlips.value = Lce.error(e.message!!)
        }
    }

    private val _downloadPaySlip = MutableLiveData<Lce<List<Payslip>>>()
    val downloadPaySlip: LiveData<Lce<List<Payslip>>> = _downloadPaySlip

    fun downloadPaySlip(
        payslip: Payslip
    ) = viewModelScope.launch {
        _downloadPaySlip.value = Lce.loading()

        try {
            val paySlips = walletRepository.getPaySlips()
            _downloadPaySlip.value = Lce.content(paySlips)
        } catch (e: Exception) {
            _downloadPaySlip.value = Lce.error(e.message!!)
        }
    }



    override fun onCleared() {
        super.onCleared()
        profileListenerRegistration?.remove()
    }
}