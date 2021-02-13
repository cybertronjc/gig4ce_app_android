package com.gigforce.client_activation.client_activation.models

import com.gigforce.core.core.base.basefirestore.BaseFirestoreDataModel

data class Skill(
    var id: String = ""
): BaseFirestoreDataModel(tableName = "skills"){
}