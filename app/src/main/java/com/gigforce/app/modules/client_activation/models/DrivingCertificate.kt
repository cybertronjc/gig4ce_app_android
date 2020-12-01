package com.gigforce.app.modules.client_activation.models

data class DrivingCertificate(
        val frontImage: String? = null,
        @JvmField val verified: Boolean = false,
        var slotBooked: Boolean = false,
        var status: String = "",
        var partnerSchoolDetails: PartnerSchoolDetails? = null, var selectedDate: String = "", var selectedTime: String = "",
        var subDLChequeInSameCentre: Boolean? = null
)