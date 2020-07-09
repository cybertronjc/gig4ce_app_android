package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep

@Keep
data class GigContactDetails(
    var contactName: String? = null,
    var mobileNo: String? = null
)