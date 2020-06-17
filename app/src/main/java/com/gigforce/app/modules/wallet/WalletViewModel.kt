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
        //getUserWallet()

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