package com.gigforce.wallet.models

import com.gigforce.core.fb.BaseFirestoreDataModel

data class QA(
    var question: String = "",
    var answer: String = ""
): BaseFirestoreDataModel(tableName = "qas_wallet") {
}