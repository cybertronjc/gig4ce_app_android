package com.gigforce.app.modules.profile

interface ProfileCardBgCallbacks {
    fun checked(isChecked: Boolean, contact: String)
    fun editNumber(number: String, isWhatsApp: Boolean)
}