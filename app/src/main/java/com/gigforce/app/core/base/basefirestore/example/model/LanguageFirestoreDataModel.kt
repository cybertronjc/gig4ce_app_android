package com.gigforce.app.core.base.basefirestore.example.model

import com.gigforce.app.core.base.basefirestore.BaseFirestoreDataModel

class LanguageFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        name: String = "",
        speakingSkill: String = "",
        writingSkill: String = ""
    ):super("Language")
}