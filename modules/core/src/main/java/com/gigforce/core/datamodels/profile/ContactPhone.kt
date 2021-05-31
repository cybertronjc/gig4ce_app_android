package com.gigforce.core.datamodels.profile

data class ContactPhone(
    var phone: String? = null,
    var isVerified: Boolean = false,
    var isWhatsapp: Boolean = false

){}