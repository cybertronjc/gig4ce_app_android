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

    data class JoiningListRecyclerJoiningItemData(
        val userUid: String,
        val userName: String,
        val userProfilePicture: String,
        val userProfilePhoneNumber: String,
        val userProfilePictureThumbnail: String,
        val status: String,
        val joiningStatusText: String
    ): JoiningListRecyclerItemData(
        LeadActivationViewTypes.JoiningList
    )
}