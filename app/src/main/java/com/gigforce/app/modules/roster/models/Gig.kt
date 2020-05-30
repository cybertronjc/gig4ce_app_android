package com.gigforce.app.modules.roster.models

import com.google.firebase.Timestamp

data class Gig (
    var tag: String = "",
    var gigerId: String = "",
    var startDateTime: Timestamp? = null,
    var title: String = "",
    var gigStatus: String = "upcoming",
    var isGigCompleted: Boolean = false,
    var isPaymentDone: Boolean = false,
    //var startHour: Int = 0,
    //var startMinute: Int = 0,
    var duration: Float = 0.0F
) {
    init {
            startDateTime ?.let {
                startHour = startDateTime!!.toDate().hours
                startMinute = startDateTime!!.toDate().minutes
            }
        }

    var startHour: Int = 0
        get() = startDateTime!!.toDate().hours

    var startMinute: Int = 0
        get() = startDateTime!!.toDate().minutes

    //fun getStartHour(): Int {
      //  return startDateTime!!.toDate().hours
    //}
}

