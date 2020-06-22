package com.gigforce.app.modules.wallet

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.profile.ProfileFirebaseRepository
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.modules.wallet.models.Wallet
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener

class WalletViewModel: ViewModel() {

    companion object {
        fun newInstance() = WalletViewModel()
    }

    var walletRepository = WalletfirestoreRepository()
    var userWallet: MutableLiveData<Wallet> = MutableLiveData<Wallet>()

    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData()

    var profileFirebaseRepository = ProfileFirebaseRepository()
    fun getProfileData() {
        profileFirebaseRepository.getDBCollection()
            .addSnapshotListener(EventListener<DocumentSnapshot> { value, e ->
                if (e != null) {
                    Log.w("ProfileViewModel", "Listen failed", e)
                    return@EventListener
                }

                if (value!!.data == null) {
                    profileFirebaseRepository.createEmptyProfile()
                } else {
                    Log.d("ProfileViewModel", value!!.data.toString())
                    userProfileData.postValue(
                        value!!.toObject(ProfileData::class.java)
                    )
                    Log.d("ProfileViewModel", userProfileData.toString())
                }
            })
    }
    init {
        //getUserWallet()
        getProfileData()

        userWallet.value = Wallet(
            balance = 450,
            isMonthlyGoalSet = true,
            monthlyGoalLimit = 5000,
            monthlyEarnedAmount = 4000
        )
    }

    private fun getUserWallet() {
        walletRepository.getDBCollection().addSnapshotListener { value, e ->
            if (value!!.data == null) {
                walletRepository.setDefaultData(Wallet(balance = 0))
            }
            else {
                userWallet.postValue(value.toObject(Wallet::class.java))
            }
        }
    }
}