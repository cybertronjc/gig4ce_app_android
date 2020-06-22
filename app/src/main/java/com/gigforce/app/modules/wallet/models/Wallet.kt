package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

data class Wallet(
    var balance: Int = 0,
    var isMonthlyGoalSet: Boolean = false,
    var monthlyGoalLimit: Int = 0,
    var monthlyEarnedAmount: Int = 0

): BaseFirestoreDataModel(tableName = "wallets") {
}