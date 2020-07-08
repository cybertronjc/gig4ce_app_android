package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp

@Keep
data class Gig(
        var gigId: String,
        var gigerId: String,
        var gigAmount: Double = 0.0,
        var title: String,
        var startDate: Timestamp,
        var gigStatus: String = "upcoming",
        var companyName: String?,
        var contactNo: String?,
        var isGigCompleted: Boolean = false,
        var isPaymentDone: Boolean = false,
        var duration: Float = 0.0F,
        var gigRating: Float = 0.0F,
        var gigType: String?,
        var gigDetails: GigDetails?,
        var gigHighLights: List<String>,
        var gigRequirements: List<String>,
        var gigLocationDetails: GigLocationDetails?,
        var attendance: GigAttendance?,
        var gigContactDetails: GigContactDetails
)