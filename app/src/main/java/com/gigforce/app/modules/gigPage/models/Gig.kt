package com.gigforce.app.modules.gigPage.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.ZoneId

@Keep
data class Gig(
    var gigId: String = "",
    var gigerId: String = "",
    var gigAmount: Double = 0.0,
    var title: String = "",
    var address: String = "",
    var latitude: Double? = null,
    var longitude: Double? = null,
    var startDateTime: Timestamp? = null,
    var endDateTime: Timestamp? = null,
    var gigStatus: String = "upcoming",
    var companyLogo: String? = null,
    var companyName: String? = null,
    var contactNo: String? = null,
    var isFavourite: Boolean = false,
    var isGigCompleted: Boolean = false,
    var isPaymentDone: Boolean = false,
    var duration: Float = 0.0F,
    var gigRating: Float = 0.0F,
    var locationPictures: List<String> = emptyList(),
    var ratingUserReceived: Float = -1.0F,
    var gigType: String? = null,
    var gigHighLights: List<String> = emptyList(),
    var gigRequirements: List<String> = emptyList(),
    var attendance: GigAttendance? = null,
    var gigContactDetails: GigContactDetails? = null
){

    fun isGigOfToday(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isEqual(currentDate)
    }

    private fun isGigOfFuture(): Boolean {
        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isAfter(currentDate)
    }

    private fun isGigOfPast(): Boolean {

        val gigDate =
            startDateTime!!.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        val currentDate = LocalDate.now()
        return gigDate.isBefore(currentDate)
    }

}