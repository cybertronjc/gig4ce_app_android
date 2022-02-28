package com.gigforce.common_ui.viewmodels.payouts

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Parcelize
data class Payout(

    @field:SerializedName("paymentCycleEndDate")
    val paymentCycleEndDate: String? = null,

    @field:SerializedName("amount")
    val amount: Double? = null,

    @field:SerializedName("businessIcon")
    val businessIcon: String? = null,

    @field:SerializedName("businessName")
    val businessName: String? = null,

    @field:SerializedName("businessid")
    val businessid: String? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("statusColorCode")
    val statusColorCode: String? = null,

    @field:SerializedName("category")
    val category: String? = null,

    @field:SerializedName("paidOnDate")
    val paidOnDate: String? = null,

    @field:SerializedName("status")
    val status: String? = null,

    @field:SerializedName("helpLineNumber")
    val helpLineNumber: String? = null
) : Parcelable{

    private val isoDateFormatter = DateTimeFormatter.ISO_LOCAL_DATE //YYYY-MM-DD
    private val endCycleMonthYearFormatter = DateTimeFormatter.ofPattern("LLL yyyy") //YYYY-MM-DD
    private val paidOnDateFormatter = DateTimeFormatter.ofPattern("dd/LL/yyyy") //YYYY-MM-DD

    fun getPaymentCycleEndDateMonthYear(): String {
        return if (this.paymentCycleEndDate == null) {
            "Others"
        } else {
            endCycleMonthYearFormatter.format(isoDateFormatter.parse(this.paymentCycleEndDate))
        }
    }

    fun getPaidOnDateString(): String {
        return if (this.paidOnDate == null) {
            "-"
        } else {
            paidOnDateFormatter.format(isoDateFormatter.parse(this.paidOnDate))
        }
    }

}
