package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

data class Wallet(
    var balance: Int = 0
): BaseFirestoreDataModel(tableName = "wallets") {
}