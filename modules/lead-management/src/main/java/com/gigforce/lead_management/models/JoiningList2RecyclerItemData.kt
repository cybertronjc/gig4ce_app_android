package com.gigforce.lead_management.models

import com.gigforce.common_ui.viewdatamodels.leadManagement.JoiningBusiness
import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.ui.joining_list_2.JoiningList2ViewModel
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class JoiningList2RecyclerItemData(
    val type: Int
) : SimpleDVM(type) {

    data class JoiningListRecyclerStatusItemData(
        val status: String,
        val dropEnabled: Boolean
    ) : JoiningList2RecyclerItemData(
        LeadActivationViewTypes.JoiningListStatus
    )

    data class JoiningListRecyclerBusinessItemData(
        val buisnessName: String
    ) : JoiningList2RecyclerItemData(
        LeadActivationViewTypes.JoiningBusiness
    )

    data class JoiningListRecyclerJoiningItemData(
        val _id :String,
        val assignGigsFrom: String?,
        val gigerName: String,
        val gigerMobileNo: String,
        val gigerId: String,
        val profilePicture: String?,
        val bussiness: JoiningBusiness,
        val status: String,
        var selected: Boolean,
        val createdAt: String?,
        val updatedAt: String?,
        val isVisible: Boolean,
        val isActive: Boolean,
        val selectEnable: Boolean,
        val viewModel: JoiningList2ViewModel
    ): JoiningList2RecyclerItemData(
        LeadActivationViewTypes.JoiningList2
    )
}