package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.lead_management.views.LeadActivationViewTypes

open class ApplicationChecklistRecyclerItemData(
    val type: Int
) : SimpleDVM(type, onClickNavPath = null) {

    data class ApplicationChecklistItemData(
        val checkName: String,
        var gigerUid: String?=null,
        val gigerName: String,
        val status: String,
        val docType: String,
        val isOptional: Boolean,
        val frontImage: String?,
        val backImage: String?
    ): ApplicationChecklistRecyclerItemData(
        LeadActivationViewTypes.GigerInfo
    )
}