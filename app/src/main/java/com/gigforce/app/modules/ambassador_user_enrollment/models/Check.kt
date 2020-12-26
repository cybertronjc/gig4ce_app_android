package com.gigforce.app.modules.ambassador_user_enrollment.models

import com.gigforce.app.utils.Lang
import com.gigforce.app.utils.lang_models.BaseLangModel

data class Check(@Lang("check_") var check_: String = "") : BaseLangModel()