package com.gigforce.app.core.base.example.model

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

class AchievementFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        title: String = "",
        issuingAuthority: String = "",
        location: String = "",
        year: String = ""
    ):super("Achievement"){

    }
}