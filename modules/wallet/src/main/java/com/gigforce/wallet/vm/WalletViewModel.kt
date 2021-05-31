package com.gigforce.wallet.vm

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.wallet.models.Wallet
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.core.di.repo.IProfileFirestoreRepository
import com.gigforce.wallet.WalletfirestoreRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.EventListener
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(private val profileFirebaseRepository: IProfileFirestoreRepository) : ViewModel() {

    var walletRepository = WalletfirestoreRepository()
    var userWallet: MutableLiveData<Wallet> = MutableLiveData<Wallet>()

    var userProfileData: MutableLiveData<ProfileData> = MutableLiveData()

    //    var profileFirebaseRepository = ProfileFirebaseRepository()
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
                    Log.d("ProfileViewModel", value.data.toString())
                    userProfileData.postValue(
                        value.toObject(ProfileData::class.java)
                    )
                    Log.d("ProfileViewModel", userProfileData.toString())
                }
            })
    }
    init {
        getUserWallet()
        getProfileData()
    }

    private fun getUserWallet() {
        walletRepository.getDBCollection().addSnapshotListener { value, e ->
            if (value!!.data == null) {
                walletRepository.setDefaultData(Wallet(balance = 0F))
            } else {
                userWallet.postValue(value.toObject(Wallet::class.java))
            }
        }
    }

}