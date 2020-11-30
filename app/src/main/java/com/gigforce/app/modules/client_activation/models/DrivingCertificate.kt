package com.gigforce.app.modules.client_activation.models

data class DrivingCertificate(
        val frontImage: String? = null,
        @JvmField val verified: Boolean = false
)