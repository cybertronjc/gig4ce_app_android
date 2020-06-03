package com.gigforce.app.modules.wallet

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.gigforce.app.modules.wallet.models.Wallet

class WalletViewModel: ViewModel() {

    companion object {
        fun newInstance() = WalletViewModel()
    }

    var walletRepository = WalletfirestoreRepository()
    var userWallet: MutableLiveData<Wallet> = MutableLiveData<Wallet>()

    init {
        getUserWallet()
    }

    private fun getUserWallet() {
        walletRepository.getUserWallet().addSnapshotListener { value, e ->
            value?.let {
                userWallet.postValue(it.toObject(Wallet::class.java))
            }
        }
    }
}