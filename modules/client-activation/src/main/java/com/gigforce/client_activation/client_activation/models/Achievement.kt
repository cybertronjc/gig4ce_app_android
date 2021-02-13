package com.gigforce.client_activation.client_activation.models

import com.gigforce.core.core.base.basefirestore.BaseFirestoreDataModel

data class Achievement(
    var title: String = "",
    var issuingAuthority: String = "",
    var location: String = "",
    var year: String = ""
): BaseFirestoreDataModel(tableName = "achievements"){
}