package com.gigforce.app.modules.profile.models

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

data class Skill(
    var id: String = ""
): BaseFirestoreDataModel(tableName = "skills"){
}