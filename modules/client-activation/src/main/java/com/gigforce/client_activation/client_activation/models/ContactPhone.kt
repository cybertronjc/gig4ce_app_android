package com.gigforce.client_activation.client_activation.models

data class ContactPhone(
    var phone: String? = null,
    var isVerified: Boolean = false,
    var isWhatsapp: Boolean = false

){}