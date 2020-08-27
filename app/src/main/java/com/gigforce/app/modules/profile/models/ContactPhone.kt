package com.gigforce.app.modules.profile.models

data class ContactPhone(
    var phone: String? = null,
    var isVerified: Boolean = false,
    var isWhatsapp: Boolean = false

){}