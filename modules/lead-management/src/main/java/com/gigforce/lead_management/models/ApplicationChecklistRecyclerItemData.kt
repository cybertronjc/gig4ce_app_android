package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class ApplicationChecklistRecyclerItemData(
    val type: Int
) : SimpleDVM(type, onClickNavPath = null) {

    data class ApplicationChecklistItemData(
        val checkName: String,
        val status: String,
        val isOptional: Boolean
    ): ApplicationChecklistRecyclerItemData(
        LeadActivationViewTypes.GigerInfo
    )
}