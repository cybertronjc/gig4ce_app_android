package com.gigforce.wallet.payouts.payout_list

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.time.LocalDate

@Parcelize
data class PayoutDateFilter(
    val id: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val textForDate: String
) : Parcelable {
    val startEndDatePair = startDate to endDate
}

object PayoutDateFilters {

    val LAST_SIX_MONTHS = PayoutDateFilter(
        id = "LAST_SIX_MONTHS",
        startDate = LocalDate.now().run {
            minusMonths(6L)
        },
        endDate = LocalDate.now(),
        textForDate = "Last six months"
    )

    val LAST_ONE_YEAR = PayoutDateFilter(
        id = "LAST_ONE_YEAR",
        startDate = LocalDate.now().run {
            minusYears(1L)
        },
        endDate = LocalDate.now(),
        textForDate = "Last one year"
    )

    val LAST_FIVE_YEARS = PayoutDateFilter(
        id = "LAST_FIVE_YEARS",
        startDate = LocalDate.now().run {
            minusYears(5L)
        },
        endDate = LocalDate.now(),
        textForDate = "Last five years"
    )

}