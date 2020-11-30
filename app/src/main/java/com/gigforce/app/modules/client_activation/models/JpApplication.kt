package com.gigforce.app.modules.client_activation.models

import java.util.*

data class JpApplication(var id: String = "", var JPId: String = "", var approvedBy: String = "", var approvedOn: String = "", var gigerId: String = "",
                         var rejectedBy: String = "", var rejectedOn: String = "", var status: String = "draft", var stepDone: Int = 1, var stepsTotal: Int = 0, var
                         submitOn: String = "", var draft: MutableList<JpDraft> = mutableListOf(), var process: MutableList<JpProcess> = mutableListOf(), var applyOn: Date = Date(),
                         var questionnaireSubmission: List<QuestionsSubmission> = listOf(),
                         var drivingCert: DrivingCertificate? = null,
                         var partnerSchoolDetails: PartnerSchoolDetails? = null, var selectedDate: String = "", var selectedTime: String = "",
                         var subDLChequeInSameCentre: Boolean? = null, var slotBooked: Boolean = false

)