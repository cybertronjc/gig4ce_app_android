package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude

data class Language(
    var name: String = "",
    var speakingSkill: String = "0",
    var writingSkill: String = "0",
    var isMotherLanguage: Boolean = false,
    @get:Exclude var validateFields: Boolean = false
) : BaseFirestoreDataModel(tableName = "languages") {
}