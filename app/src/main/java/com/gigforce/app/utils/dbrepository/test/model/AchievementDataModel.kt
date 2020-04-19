package com.gigforce.app.utils.dbrepository.test.model

import com.gigforce.app.utils.dbrepository.BaseDataModel

class AchievementDataModel:BaseDataModel{
    constructor(
        title: String = "",
        issuingAuthority: String = "",
        location: String = "",
        year: String = ""
    ):super("Achievement"){

    }
}