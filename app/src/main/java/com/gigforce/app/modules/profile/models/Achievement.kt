package com.gigforce.app.modules.profile.models

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

data class Achievement(
    var title: String = "",
    var issuingAuthority: String = "",
    var location: String = "",
    var year: String = ""
): BaseFirestoreDataModel(tableName = "achievements"){
}