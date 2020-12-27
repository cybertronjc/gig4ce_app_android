package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.gigforce.app.utils.CheckCollectionTranslation
import com.gigforce.app.utils.TranslationNeeded
import com.gigforce.app.utils.CheckNestedTranslation
import com.gigforce.app.utils.lang_models.BaseLangModel

data class AmbassadorProfiles(
    var id: String = "",
    @field:TranslationNeeded("payoutNote") var payoutNote: String = "",
    @field:TranslationNeeded("actionButtonText") var actionButtonText: String = "",
    @field:TranslationNeeded("ambassadorAnswer") var ambassadorAnswer: String = "",
    @field:TranslationNeeded("ambassadorQuestion") var ambassadorQuestion: String = "",
    @field:TranslationNeeded("responsibilitiesTitle") var responsibilitiesTitle: String = "",
    @field:TranslationNeeded("subTitle") var subTitle: String = "",
    @field:TranslationNeeded("title") var title: String = "",
    @field:TranslationNeeded("ambassadorCardActionText") var ambassadorCardActionText: String = "",
    @field:TranslationNeeded("ambassadorCardSubTitle") var ambassadorCardSubTitle: String = "",
    @field:TranslationNeeded("ambassadorCardTitle") var ambassadorCardTitle: String = "",
    @field:TranslationNeeded("responsibilities") var responsibilities: List<String> = listOf()


) :
    BaseLangModel()