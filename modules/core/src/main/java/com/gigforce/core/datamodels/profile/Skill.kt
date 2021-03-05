package com.gigforce.core.datamodels.profile

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

data class Skill(
    var id: String = ""
): BaseFirestoreDataModel(tableName = "skills"){
}