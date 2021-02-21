package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.gigforce.core.TranslationNeeded
import com.gigforce.app.utils.lang_models.BaseLangModel

data class Responsibilities(@field:TranslationNeeded("text") var text: String = "") :
    BaseLangModel()