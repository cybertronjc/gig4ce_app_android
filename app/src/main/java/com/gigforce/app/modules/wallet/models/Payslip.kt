package com.gigforce.app.modules.wallet.models

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import java.time.Month
import java.time.MonthDay
import java.util.*

data class Payslip(

    @get:PropertyName("accountNo")
    @set:PropertyName("accountNo")
    var accountNo: String = "",

    @get:PropertyName("dateOfPayment")
    @set:PropertyName("dateOfPayment")
    var dateOfPayment: String = "",


    @get:PropertyName("ifsc")
    @set:PropertyName("ifsc")
    var ifsc: String = "",

    @get:PropertyName("location")
    @set:PropertyName("location")
    var location: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("profile")
    @set:PropertyName("profile")
    var profile: String = "",

    @get:PropertyName("serialNumber")
    @set:PropertyName("serialNumber")
    var serialNumber: String = "",


    @get:PropertyName("totalPayout")
    @set:PropertyName("totalPayout")
    var totalPayout: Double = 0.0,

    @get:PropertyName("variablePayout")
    @set:PropertyName("variablePayout")
    var variablePayout: Double = 0.0,

    @get:PropertyName("fixedPayout")
    @set:PropertyName("fixedPayout")
    var fixedPayout: Double = 0.0,


    @get:PropertyName("monthOfPayment")
    @set:PropertyName("monthOfPayment")
    var monthOfPayment: String = "",

    @get:PropertyName("uid")
    @set:PropertyName("uid")
    var uid: String = "",

    @get:PropertyName("yearOfPayment")
    @set:PropertyName("yearOfPayment")
    var yearOfPayment: Int = -1,

    @get:PropertyName("pdfDownloadLink")
    @set:PropertyName("pdfDownloadLink")
    var pdfDownloadLink: String? = null
) {

    fun getMonthNo() : Int{
       return Month.valueOf(monthOfPayment.toUpperCase(Locale.getDefault())).value
    }

    @Exclude
    var loading :Boolean = false
}