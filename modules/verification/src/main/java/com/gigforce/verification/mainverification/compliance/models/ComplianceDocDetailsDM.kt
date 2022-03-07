package com.gigforce.verification.mainverification.compliance.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class DataListItem(
    val name: String,
    val link: String
)

data class ComplianceDocDetailsDM(
    val type: String = "",
    val name: String = "",
    val value: String = "",
    val path: String? = null,
    val data: List<DataListItem>? = null
): SimpleDVM(
    CommonViewTypes.COMPLIANCE_CARD)
