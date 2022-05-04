package com.gigforce.common_ui.navigation.lead_management

import androidx.core.os.bundleOf
import com.gigforce.common_ui.navigation.LeadManagementNavDestinations
import com.gigforce.common_ui.viewdatamodels.leadManagement.ChangeTeamLeaderRequestItem
import com.gigforce.common_ui.viewdatamodels.leadManagement.DropScreenIntentModel
import com.gigforce.core.navigation.INavigation
import javax.inject.Inject

class LeadManagementNavigation @Inject constructor(
    private val navigation: INavigation
) {

    fun openChangeTLBottomSheet(
        changeTLRequest: ArrayList<ChangeTeamLeaderRequestItem>
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_CHANGE_TL,
            bundleOf(
                "gigers_list" to changeTLRequest
            )
        )
    }

    fun openDropJoiningOrGigerScreen(
        dropScreenIntentData: DropScreenIntentModel
    ) {
        navigation.navigateTo(
            LeadManagementNavDestinations.FRAGMENT_JOINING_DROP_SELECTION_2,
            bundleOf(
                "selections_to_drop" to arrayListOf(dropScreenIntentData)
            )
        )
    }
}