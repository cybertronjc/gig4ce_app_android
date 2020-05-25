package com.gigforce.app.modules.profile.models

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel
import java.util.*

data class Language(
    var name: String = "",
    var speakingSkill: String = "",
    var writingSkill: String = "",
    var isMotherLanguage: Boolean = false
): BaseFirestoreDataModel(tableName = "languages"){
}