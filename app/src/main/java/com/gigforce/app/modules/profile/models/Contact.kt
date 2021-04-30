package com.gigforce.app.modules.profile.models

import com.google.firebase.firestore.Exclude

data class Contact(
    var phone: String = "",
    var email: String = "",
    var isWhatsApp: Boolean = false,
    @get:Exclude var validateFields: Boolean = false
) {
}