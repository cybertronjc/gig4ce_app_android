package com.gigforce.app.modules.wallet.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

class Wallet(
    var balance: Int = 0
): BaseFirestoreDataModel(tableName = "wallets") {
}