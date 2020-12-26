package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.gigforce.app.utils.Lang
import com.gigforce.app.utils.NestedLang
import com.gigforce.app.utils.lang_models.BaseLangModel

data class Role(
    var answer: String = "",
    @Lang("label") var label: String = "",
    @NestedLang var check: Check? = null
) : BaseLangModel()