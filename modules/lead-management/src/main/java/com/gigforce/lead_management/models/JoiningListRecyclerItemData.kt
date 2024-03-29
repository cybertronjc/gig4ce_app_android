package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class JoiningListRecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class JoiningListRecyclerStatusItemData(
        val status: String
    ) : JoiningListRecyclerItemData(
        LeadActivationViewTypes.JoiningListStatus
    )

    data class JoiningListRecyclerBusinessItemData(
        val buisnessName: String
    ) : JoiningListRecyclerItemData(
        LeadActivationViewTypes.JoiningBusiness
    )

    data class JoiningListRecyclerJoiningItemData(
        val joiningId :String,
        val userUid: String?,
        val userName: String,
        val userProfilePicture: String,
        val userProfilePhoneNumber: String,
        val userProfilePictureThumbnail: String,
        val status: String,
        val joiningStatusText: String,
        val jobProfileId : String,
        val jobProfileName : String,
        val jobProfileIcon : String,
        val tradeName : String
    ): JoiningListRecyclerItemData(
        LeadActivationViewTypes.JoiningList2
    )
}