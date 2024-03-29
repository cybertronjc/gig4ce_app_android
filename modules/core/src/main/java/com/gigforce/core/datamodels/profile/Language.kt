package com.gigforce.core.datamodels.profile

import com.gigforce.core.fb.BaseFirestoreDataModel
import com.google.firebase.firestore.Exclude

data class Language(
    var name: String = "",
    var speakingSkill: String = "0",
    var writingSkill: String = "0",
    var isMotherLanguage: Boolean = false,
    @get:Exclude var validateFields: Boolean = false
) : BaseFirestoreDataModel(tableName = "languages") {
}