package com.gigforce.lead_management.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.client_activation.Dependency
import com.gigforce.lead_management.views.LeadActivationViewTypes
import com.google.gson.annotations.SerializedName

open class ApplicationChecklistRecyclerItemData(
    val type: Int
) : SimpleDVM(type, onClickNavPath = null) {

    data class ApplicationChecklistItemData(
        val checkName: String,
        var gigerUid: String? = null,
        val gigerName: String? = null,
        val status: String,
        val docType: String,
        val isOptional: Boolean,
        val frontImage: String?,
        val backImage: String?,
        val checkListItemType : String?,
        val options : Dependency?,
        val courseId: String?,
        val moduleId: String?,
        var title: String?,
    ): ApplicationChecklistRecyclerItemData(
        LeadActivationViewTypes.GigerInfo
    )
}