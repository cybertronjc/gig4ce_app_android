package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

data class Skill(
    var id: String = ""
): BaseFirestoreDataModel(tableName = "skills"){
}