package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

data class Language(
    var name: String = "",
    var speakingSkill: String = "0",
    var writingSkill: String = "0",
    var isMotherLanguage: Boolean = false,
    @Transient var validateFields: Boolean = false
) : BaseFirestoreDataModel(tableName = "languages") {
}