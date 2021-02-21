package com.gigforce.app.modules.wallet.models

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

data class QA(
    var question: String = "",
    var answer: String = ""
): BaseFirestoreDataModel(tableName = "qas_wallet") {
}