package com.gigforce.app.modules.wallet.models

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

data class Wallet(
    var balance: Float = 0F,
    var isMonthlyGoalSet: Boolean = false,
    var monthlyGoalLimit: Int = 0,
    var monthlyEarnedAmount: Int = 0

): BaseFirestoreDataModel(tableName = "wallets") {
}