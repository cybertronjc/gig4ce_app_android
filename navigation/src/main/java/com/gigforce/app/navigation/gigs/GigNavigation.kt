package com.gigforce.app.navigation.gigs

import androidx.core.os.bundleOf
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.navigation.NavigationOptions
import java.time.LocalDate
import javax.inject.Inject

class GigNavigation @Inject constructor(
    private val navigation : INavigation
) {

    companion object{

        const val NAV_DESTINATION_GIG_PAGE  = "gig/attendance"

        const val INTENT_EXTRA_ROLE = "role"
        const val INTENT_EXTRA_COMPANY_NAME = "company_name"
        const val INTENT_EXTRA_COMPANY_LOGO = "company_logo"
        const val INTENT_EXTRA_SELECTED_DATE = "selected_month_year"
        const val INTENT_EXTRA_GIG_ORDER_ID = "gig_order_id"
    }

    fun openGigPage(
        gigId : String
    ) = navigation.navigateTo(
        NAV_DESTINATION_GIG_PAGE,
        bundleOf("gig_id" to gigId),
        NavigationOptions.getNavOptions()
    )

    fun openGigAttendanceHistoryScreen(
        gigDate : LocalDate,
        gigTitle : String,
        gigOrderId : String,
        companyLogo : String,
        companyName : String
    ){
        navigation.navigateTo(
            "gig/gigMonthlyAttendanceFragment", bundleOf(
                INTENT_EXTRA_SELECTED_DATE to gigDate,
                INTENT_EXTRA_COMPANY_LOGO to companyLogo,
                INTENT_EXTRA_COMPANY_NAME to companyName,
                INTENT_EXTRA_GIG_ORDER_ID to gigOrderId,
                INTENT_EXTRA_ROLE to gigTitle
            ),
            NavigationOptions.getNavOptions()
        )
    }
}