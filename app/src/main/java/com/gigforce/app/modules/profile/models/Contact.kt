package com.gigforce.app.modules.profile.models

data class Contact(
    var phone: String = "",
    var email: String = "",
    var isWhatsApp: Boolean = false,
    var validateFields: Boolean = false
) {
}