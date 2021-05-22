package com.gigforce.landing_screen.landingscreen

import android.os.Bundle
import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
//import com.gigforce.app.R
import com.gigforce.landing_screen.R
import kotlin.Boolean
import kotlin.Int
import kotlin.String

class LandingScreenFragmentDirections private constructor() {
    private data class OpenRoleDetailsHome(val ROLEID: String = "\"\"", val ROLEVIADEEPLINK: Boolean
            = false) : NavDirections {
        override fun getActionId(): Int = R.id.open_role_details_home

        override fun getArguments(): Bundle {
            val result = Bundle()
            result.putString("ROLE_ID", this.ROLEID)
            result.putBoolean("ROLE_VIA_DEEPLINK", this.ROLEVIADEEPLINK)
            return result
        }
    }

    companion object {
        fun actionLandinghomefragmentToGigerVerificationFragment(): NavDirections =
                ActionOnlyNavDirections(R.id.action_landinghomefragment_to_gigerVerificationFragment)

        fun openRoleDetailsHome(ROLEID: String = "\"\"", ROLEVIADEEPLINK: Boolean = false):
                NavDirections = OpenRoleDetailsHome(ROLEID, ROLEVIADEEPLINK)
    }
}
