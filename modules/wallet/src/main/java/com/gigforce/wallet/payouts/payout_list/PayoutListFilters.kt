package com.gigforce.wallet.payouts.payout_list

import java.time.LocalDate

object PayoutListFilters {

    val LAST_SIX_MONTHS = LocalDate.now().apply {
        minusMonths(6L)
    } to LocalDate.now()

    val LAST_ONE_YEAR = LocalDate.now().apply {
        minusYears(1L)
    } to LocalDate.now()
}