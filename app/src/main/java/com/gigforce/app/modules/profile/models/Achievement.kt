package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

data class Achievement(
    var title: String = "",
    var issuingAuthority: String = "",
    var location: String = "",
    var year: String = ""
): BaseFirestoreDataModel(tableName = "achievements"){
}