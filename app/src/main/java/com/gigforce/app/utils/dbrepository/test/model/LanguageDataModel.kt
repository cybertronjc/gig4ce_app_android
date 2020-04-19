package com.gigforce.app.utils.dbrepository.test.model

import com.gigforce.app.utils.dbrepository.BaseDataModel

class LanguageDataModel:BaseDataModel{
    constructor(
        name: String = "",
        speakingSkill: String = "",
        writingSkill: String = ""
    ):super("Language")
}