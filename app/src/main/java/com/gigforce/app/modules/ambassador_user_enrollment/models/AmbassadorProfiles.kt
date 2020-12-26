package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.gigforce.app.utils.Lang
import com.gigforce.app.utils.NestedLang
import com.gigforce.app.utils.lang_models.BaseLangModel
import com.gigforce.app.utils.lang_models.LangMapSingleton
import kotlin.reflect.full.declaredMembers

data class AmbassadorProfiles(
    val ambassadorProfileKey: String = "",
    @field:Lang(langKey = "payoutNote") val payoutNote: String = "",
    @field:NestedLang var role: Role? = null

) :
    BaseLangModel() {
    // initializer block


}
