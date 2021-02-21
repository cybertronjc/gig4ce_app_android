package com.gigforce.app.core.base.example.model

import com.gigforce.core.base.basefirestore.BaseFirestoreDataModel

class LanguageFirestoreDataModel:
    BaseFirestoreDataModel {
    constructor(
        name: String = "",
        speakingSkill: String = "",
        writingSkill: String = ""
    ):super("Language")
}